package ch.picard.ppimapbuilder.layout;

import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.client.web.QuickGOClient;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.*;
import java.util.concurrent.*;

public class PMBGOSlimLayoutTask extends AbstractTask {

	private final CyNetwork network;
	private final CyNetworkView view;
	private final CyLayoutAlgorithmManager layoutManager;

	private ListSingleSelection<String> goSlim;

	@Tunable(description = "Choose a GO slim :")
	public ListSingleSelection<String> getGoSlim() {
		return goSlim;
	}

	public void setGoSlim(ListSingleSelection<String> goSlim) {
		this.goSlim = goSlim;
	}

	public PMBGOSlimLayoutTask(CyNetwork network, CyNetworkView view, CyLayoutAlgorithmManager layoutManager) {
		this.network = network;
		this.view = view;
		this.layoutManager = layoutManager;
		this.goSlim = new ListSingleSelection<String>(GOSlimRepository.getInstance().getGOSlimNames());
	}

	private final ExecutorServiceManager executorServiceManager =
			new ExecutorServiceManager(ExecutorServiceManager.DEFAULT_NB_THREAD * 3);

	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		monitor.setTitle("Fetching slimmed Gene Ontology");
		monitor.setProgress(0.1);

		final HashMap<Protein, Set<GeneOntologyTerm>> proteinGoSlim = new HashMap<Protein, Set<GeneOntologyTerm>>();

		{ // Fetch protein GO slim
			final QuickGOClient.GOSlimClient goSlimClient = new QuickGOClient.GOSlimClient(
					executorServiceManager
			);

			Set<Protein> networkProteins = new HashSet<Protein>();
			for (CyNode node : network.getNodeList()) {
				networkProteins.add(
						new Protein(
								network.getRow(node).get("Uniprot_id", String.class),
								null
						)
				);
			}

			proteinGoSlim.putAll(
					goSlimClient.searchProteinGOSlim(
							GOSlimRepository.getInstance().getGOSlim(goSlim.getSelectedValue()),
							networkProteins,
							new ProgressMonitor() {
								@Override
								public void setProgress(double v) {
									monitor.setProgress(v);
								}
							}
					)
			);
		}

		{
			ArrayList<String> fullListOfGOs = new ArrayList<String>();
			ConcurrentHashMap<String, String> mapGoTerm = new ConcurrentHashMap<String, String>();

			// Fill node rows with protein GO slim
			List<CyNode> nodeList = network.getNodeList();
			for (CyNode node : nodeList) {
				final CyRow row = network.getRow(node);
				final Protein protein = new Protein(
						row.get("Uniprot_id", String.class),
						null
				);


				if (proteinGoSlim.containsKey(protein)) {
					ArrayList<String> terms = new ArrayList<String>();
					for (GeneOntologyTerm term : proteinGoSlim.get(protein)) {
						terms.add(term.getIdentifier());
						mapGoTerm.putIfAbsent(term.getIdentifier(), term.getTerm()); // Construct a map to easily retrieve GO term from ID

					}
					row.set("Go_slim", terms);
					fullListOfGOs.addAll(terms);
				}
			}
			// System.out.println(mapGoTerm);

			// Generate list of major GO in the network
			CyTable nodeTable = view.getModel().getDefaultNodeTable();

			LinkedHashMap<String, Integer> goOccurrences = new LinkedHashMap<String, Integer>(); // Stores the number of occurrences for each GO
			for (String go : fullListOfGOs) {
				goOccurrences.put(go, Collections.frequency(fullListOfGOs, go));
			}

			ValueComparator bvc =  new ValueComparator(goOccurrences);
			TreeMap<String,Integer> sortedGoOccurrences = new TreeMap<String,Integer>(bvc); // Stores the GO ordered by amount
			sortedGoOccurrences.putAll(goOccurrences);
			// System.out.println(sortedGoOccurrences);

			// Assign one major GO for each prot
			if (nodeTable.getColumn("Go_slim_group") == null) { // Create node attribute to store the cluster assignement
				nodeTable.createColumn("Go_slim_group", String.class, false);
			}
			if (nodeTable.getColumn("Go_slim_group_term") == null) { // Create node attribute to store the cluster assignement
				nodeTable.createColumn("Go_slim_group_term", String.class, false);
			}
			for (CyNode n : network.getNodeList()) {
				List<String> tempGOList = network.getRow(n).getList("Go_slim", String.class);
				if(tempGOList == null) continue;
				for (String key : sortedGoOccurrences.keySet()) { // For each GO beginning by the most frequent
					if (tempGOList.contains(key)) { // If this GO is one of those assigned to the current node
						//System.out.print(key);
						network.getRow(n).set("Go_slim_group", key); // We consider this GO as the major to cluster this node
						network.getRow(n).set("Go_slim_group_term", mapGoTerm.get(key));
						break;
					}
				}
			}
		}

		{
			// Call attribute layout
			CyLayoutAlgorithm layout = layoutManager.getLayout("attributes-layout");
			Object context = layout.createLayoutContext();
			String layoutAttribute = "Go_slim_group";
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
			
		}


	}

	@Override
	public void cancel() {
		super.cancel();
		executorServiceManager.shutdown();
	}

	class ValueComparator implements Comparator<String> {

		LinkedHashMap<String, Integer> base;
		public ValueComparator(LinkedHashMap<String, Integer> base) {
			this.base = base;
		}

		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}

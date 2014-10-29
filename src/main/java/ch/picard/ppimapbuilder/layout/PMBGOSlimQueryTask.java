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
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.*;

public class PMBGOSlimQueryTask extends AbstractTask {

	private final CyNetwork network;

	private ListSingleSelection<String> goSlim;

	@Tunable(description = "Choose a GO slim :")
	public ListSingleSelection<String> getGoSlim() {
		return goSlim;
	}

	public void setGoSlim(ListSingleSelection<String> goSlim) {
		this.goSlim = goSlim;
	}

	public PMBGOSlimQueryTask(CyNetwork network) {
		this.network = network;
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
								network.getRow(node).get("uniprot_id", String.class),
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

		{ // Fill node rows with protein GO slim
			List<CyNode> nodeList = network.getNodeList();
			for (CyNode node : nodeList) {
				final CyRow row = network.getRow(node);
				final Protein protein = new Protein(
						row.get("uniprot_id", String.class),
						null
				);

				if (proteinGoSlim.containsKey(protein)) {
					ArrayList<String> terms = new ArrayList<String>();
					for (GeneOntologyTerm term : proteinGoSlim.get(protein))
						terms.add(term.getIdentifier());
					row.set("go_slim", terms);
				}
			}
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		executorServiceManager.shutdown();
	}
}

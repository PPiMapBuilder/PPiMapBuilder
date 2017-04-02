/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.layout;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.client.web.QuickGOClient;
import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.style.PMBVisualStylesDefinition;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PMBGOSlimLayoutTask extends AbstractTask {

	private final CyNetwork network;
	private final CyNetworkView view;
	private final CyLayoutAlgorithmManager layoutManager;
	private final VisualMappingManager visualMappingManager;

	private ListSingleSelection<String> goSlim;

	@Tunable(description = "Choose a GO slim :")
	public ListSingleSelection<String> getGoSlim() {
		return goSlim;
	}

	public void setGoSlim(ListSingleSelection<String> goSlim) {
		this.goSlim = goSlim;
	}

	public PMBGOSlimLayoutTask(CyNetwork network, CyNetworkView view, CyLayoutAlgorithmManager layoutManager, VisualMappingManager visualMappingManager) {
		this.network = network;
		this.view = view;
		this.layoutManager = layoutManager;
		this.visualMappingManager = visualMappingManager;
		this.goSlim = new ListSingleSelection<String>(GOSlimRepository.getInstance().getGOSlimNames());
	}

	private final ExecutorServiceManager executorServiceManager =
			new ExecutorServiceManager(ExecutorServiceManager.DEFAULT_NB_THREAD * 3);

	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		System.out.println("## FETCH LAYOUT");
		System.out.println("#1");
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
			System.out.println(networkProteins);
			System.out.println("#2");

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

			System.out.println(proteinGoSlim);
			System.out.println("#3");
		}

		ConcurrentHashMap<String, String> mapGoTerm = new ConcurrentHashMap<String, String>();
		mapGoTerm.put("Outside any cluster", "Outside any cluster");
		ArrayList<String> usedGo = new ArrayList<String>();
		{
			monitor.setTitle("Clustering using Gene Ontology");
			ArrayList<String> fullListOfGOs = new ArrayList<String>();
			System.out.println("#4");

			// Fill node rows with protein GO slim
			List<CyNode> nodeList = network.getNodeList();
			System.out.println("#5");
			for (CyNode node : nodeList) {
				System.out.print("#6");
				final CyRow row = network.getRow(node);
				final Protein protein = new Protein(
						row.get("Uniprot_id", String.class),
						null
				);
				System.out.print("#7");


				if (proteinGoSlim.containsKey(protein)) {
					System.out.print("#8");
					ArrayList<String> terms = new ArrayList<String>();
					for (GeneOntologyTerm term : proteinGoSlim.get(protein)) {
						System.out.print("#9");
						terms.add(term.getIdentifier());
						mapGoTerm.putIfAbsent(term.getIdentifier(), term.getTerm()); // Construct a map to easily retrieve GO term from ID

					}
					System.out.print("#10");
					row.set("Go_slim", terms);
					fullListOfGOs.addAll(terms);
				}
				System.out.print("#11");
			}
			// System.out.println(mapGoTerm);
			System.out.println("#12");

			// Generate list of major GO in the network
			CyTable nodeTable = view.getModel().getDefaultNodeTable();
			System.out.println("#13");

			LinkedHashMap<String, Integer> goOccurrences = new LinkedHashMap<String, Integer>(); // Stores the number of occurrences for each GO
			for (String go : fullListOfGOs) {
				System.out.print("#14");
				goOccurrences.put(go, Collections.frequency(fullListOfGOs, go));
			}

			System.out.println("#15");
			ValueComparator bvc =  new ValueComparator(goOccurrences);
			TreeMap<String,Integer> sortedGoOccurrences = new TreeMap<String,Integer>(bvc); // Stores the GO ordered by amount
			sortedGoOccurrences.putAll(goOccurrences);
			System.out.println("#16");
			//System.out.println(sortedGoOccurrences);

			// Assign one major GO for each prot
			if (nodeTable.getColumn("Go_slim_group") == null) { // Create node attribute to store the cluster assignement
				System.out.println("#17");
				nodeTable.createColumn("Go_slim_group", String.class, false);
			}
			if (nodeTable.getColumn("Go_slim_group_term") == null) { // Create node attribute to store the cluster assignement
				System.out.println("#18");
				nodeTable.createColumn("Go_slim_group_term", String.class, false);
			}
			System.out.println("#19");
			LinkedHashMap<String, CyNode> legendAsNode = new LinkedHashMap<String, CyNode>();
			for (CyNode n : network.getNodeList()) {
				System.out.print("#20");
				List<String> tempGOList = network.getRow(n).getList("Go_slim", String.class);
				if(tempGOList == null) {
					System.out.print("#21");
					network.getRow(n).set("Go_slim_group_term", "Outside any cluster");
					
					if (!usedGo.contains("Outside any cluster")) {
						usedGo.add("Outside any cluster");
					}
				}
				else {
					System.out.print("#22");
					for (String key : sortedGoOccurrences.keySet()) { // For each GO beginning by the most frequent
						if (tempGOList.contains(key)) { // If this GO is one of those assigned to the current node
							//System.out.print(key);
							network.getRow(n).set("Go_slim_group", key); // We consider this GO as the major to cluster this node
							network.getRow(n).set("Go_slim_group_term", mapGoTerm.get(key));
							
							if (!usedGo.contains(key)) {
								usedGo.add(key);
							}
							
							break;
						}
					}
				}
				
			}
			System.out.println("#23");
		}
		
		{
			monitor.setTitle("Create legend as nodes");
			System.out.println(goSlim.getSelectedValue()); // Name of the GO slim file
			System.out.println(GOSlimRepository.getInstance().getGOSlim(goSlim.getSelectedValue())); // Full list of GO slim from file

			System.out.println("#24");
			for (String legendGo: usedGo) {
				System.out.print("#25");
				System.out.println(network.getNodeList());
				if (!network.getNodeList().contains(legendGo)) {
					System.out.println("#26");
					System.out.println(legendGo + " : " + mapGoTerm.get(legendGo));
					CyNode node = network.addNode();
					CyRow nodeAttr = network.getRow(node);
					System.out.println("#27");
					nodeAttr.set("name", legendGo);
					System.out.println("#28");
					nodeAttr.set("Gene_name", mapGoTerm.get(legendGo)); // TODO: add a "label" column containing either the gene name either the legend label
					System.out.println("#29");
					nodeAttr.set("Go_slim_group", legendGo);
					System.out.println("#30");
					nodeAttr.set("Go_slim_group_term", mapGoTerm.get(legendGo));
					System.out.println("#31");
					nodeAttr.set("Legend", "true");
					System.out.println("#32");
				}
			}
			System.out.println("#33");
		}
		
		{
			monitor.setTitle("Update view");
			System.out.println("#34");
			VisualStyle vs = PMBVisualStylesDefinition.getInstance().addVisualStyle("PMB_"+goSlim.getSelectedValue().replace(" ", "_"), goSlim.getSelectedValue());
			vs.apply(view);
			System.out.println("#35");
			visualMappingManager.setCurrentVisualStyle(vs);
			view.updateView();
			System.out.println("#36");
		}

		{
			System.out.println("#37");
			monitor.setTitle("Creating layout based on clusters");
			// Call attribute layout
			CyLayoutAlgorithm layout = layoutManager.getLayout("attributes-layout");
			System.out.println("#38");
			Object context = layout.createLayoutContext();
			String layoutAttribute = "Go_slim_group";
			System.out.println("#39");
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
		}

		System.out.println("#40");
		CyTable networkTable = network.getDefaultNetworkTable();
		if (networkTable.getColumn("layout") == null) { // Create network attribute to indicate if the layout was made
			System.out.println("#41");
			networkTable.createColumn("layout", Boolean.class, false);
		}
		System.out.println("#42");
		network.getRow(network).set("layout", true);
		System.out.println("#43");


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

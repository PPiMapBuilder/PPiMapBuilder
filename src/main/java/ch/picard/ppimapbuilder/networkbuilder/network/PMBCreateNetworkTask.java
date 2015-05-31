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
    
package ch.picard.ppimapbuilder.networkbuilder.network;

import ch.picard.ppimapbuilder.PMBActivator;
import ch.picard.ppimapbuilder.data.JSONUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.OLSClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicResultTranslator;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyCategory;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome.DeferredFetchUniProtEntryTask;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PMBCreateNetworkTask extends AbstractTask {

	// For the network
	private final CyNetworkManager networkManager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkNaming networkNaming;

	// For the view
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkViewManager networkViewManager;

	// For the layout
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;

	// For the visual style
	private final VisualMappingManager visualMappingManager;

	private final ExecutorServiceManager executorServiceManager;

	private final NetworkQueryParameters networkQueryParameters;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

	private final Set<UniProtEntry> proteinOfInterestPool;
	//Network data
	private final HashMap<UniProtEntry, CyNode> nodeNameMap;
	private final long startTime;

	public PMBCreateNetworkTask(
			final CyNetworkManager networkManager,
			final CyNetworkNaming networkNaming,
			final CyNetworkFactory networkFactory,
			final CyNetworkViewFactory networkViewFactory,
			final CyNetworkViewManager networkViewManager,
			final CyLayoutAlgorithmManager layoutAlgorithmManager,
			final VisualMappingManager visualMappingManager,
			final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			final UniProtEntrySet interactorPool,
			final Set<UniProtEntry> proteinOfInterestPool,
			final NetworkQueryParameters networkQueryParameters,
			final ExecutorServiceManager executorServiceManager, long startTime
	) {

		// For the network
		this.networkManager = networkManager;
		this.networkFactory = networkFactory;
		this.networkNaming = networkNaming;

		// For the view
		this.networkViewFactory = networkViewFactory;
		this.networkViewManager = networkViewManager;

		// For the layout
		this.layoutAlgorithmManager = layoutAlgorithmManager;

		// For the visual style
		this.visualMappingManager = visualMappingManager;

		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		this.networkQueryParameters = networkQueryParameters;
		this.executorServiceManager = executorServiceManager;

		this.nodeNameMap = new HashMap<UniProtEntry, CyNode>();

		this.startTime = startTime;
	}

	@Override
	public void run(TaskMonitor monitor) {
		monitor.setTitle("PPiMapBuilder interaction network build");
		monitor.setStatusMessage("Building Cytoscape network...");

		if (!interactionsByOrg.get(networkQueryParameters.getReferenceOrganism()).isEmpty() && !interactionsByOrg.isEmpty()) {
			CyNetwork network;
			{ // Create network, edges and nodes
				network = createNetwork();
				createNodeTable(network);
				monitor.setProgress(0.15);

				createEdges(network);
				monitor.setProgress(0.75);

				if(cancelled) return;
				if(!networkQueryParameters.isInteractomeQuery()) {
					removeNodeNotConnectedToPOIs(network);
				}
			}

			if(cancelled) return;

			CyNetworkView view;
			{ // Create view and apply layout and style
				view = applyView(network);

				// Layout
				applyLayout(view, network.getNodeCount());

				// Visual Style
				applyVisualStyle(view);
				monitor.setProgress(0.90);
			}

			if(cancelled) return;

			// Add network build time
			network.getRow(network).set(
					"build time (seconds)",
					(((int) (System.currentTimeMillis() - startTime)) / 1000)
			);

			if(networkQueryParameters.isInteractomeQuery() && !cancelled) {
				// For interactome => search uniprot entries in the background
				PMBActivator
						.getPMBBackgroundTaskManager()
						.launchTask(
								new DeferredFetchUniProtEntryTask(
										executorServiceManager,
										interactorPool,
										network
								)
						);
			}
		}
		monitor.setProgress(1.0);
	}

	private void removeNodeNotConnectedToPOIs(CyNetwork network) {
		Set<CyNode> nodes = new HashSet<CyNode>(network.getNodeList());
		Set<CyNode> POIsNodes = new HashSet<CyNode>();

		for(UniProtEntry poiEntry: proteinOfInterestPool) {
			final CyNode node = getNode(poiEntry);
			if(node != null)
				POIsNodes.add(node);
		}

		Set<CyNode> nodesLinkedToPOIs = new HashSet<CyNode>();
		nodesLinkedToPOIs.addAll(POIsNodes);
		for (CyEdge edge : network.getEdgeList()) {
			if (POIsNodes.contains(edge.getSource())) {
				nodesLinkedToPOIs.add(edge.getTarget());
			}
			if (POIsNodes.contains(edge.getTarget())) {
				nodesLinkedToPOIs.add(edge.getSource());
			}
		}

		nodes.removeAll(nodesLinkedToPOIs);
		//System.out.println(nodesLinkedToPOIs);
		network.removeNodes(nodes);
	}

	private CyNetwork createNetwork() {
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle("PPiMapBuilder network"));


		CyTable networkTable = network.getDefaultNetworkTable();
		{ // Create columns
			networkTable.createColumn("created by", String.class, true);
			networkTable.createColumn("reference organism", String.class, true);
			if(!networkQueryParameters.isInteractomeQuery()) {
				networkTable.createListColumn("proteins of interest", String.class, true);
				networkTable.createListColumn("other organisms", String.class, true);
			}
			networkTable.createListColumn("source databases", String.class, true);
			networkTable.createColumn("build time (seconds)", Integer.class, true);
		}

		{ // Fill columns
			network.getRow(network).set(
					"created by",
					"PPiMapBuilder v"
							+ PMBActivator.version
							+ " " +
							(
									PMBActivator.isSnapshot ?
											PMBActivator.buildTimestamp :
											""
							) //display PMB build timestamp in snapshots
			);
			network.getRow(network).set("reference organism", networkQueryParameters.getReferenceOrganism().getScientificName());
			if(!networkQueryParameters.isInteractomeQuery()) {
				network.getRow(network).set("proteins of interest", new ArrayList<String>(ProteinUtils.asIdentifiers(proteinOfInterestPool)));
				network.getRow(network).set("other organisms", OrganismUtils.organismsToStrings(networkQueryParameters.getOtherOrganisms()));
			}
			network.getRow(network).set("source databases", InteractionUtils.psicquicServicesToStrings(networkQueryParameters.getSelectedDatabases()));
		}
		return network;
	}

	private void createNodeTable(CyNetwork network) {
		// Node attributes
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn("Uniprot_id", String.class, false);
		nodeTable.createListColumn("Accessions", String.class, false);
		nodeTable.createColumn("Gene_name", String.class, false);
		nodeTable.createColumn("Ec_number", String.class, false);
		nodeTable.createListColumn("Synonym_gene_names", String.class, false);
		nodeTable.createColumn("Protein_name", String.class, false);
		nodeTable.createColumn("Tax_id", String.class, false);
		nodeTable.createColumn("Reviewed", String.class, false);
		nodeTable.createListColumn("Cellular_components_hidden", String.class, false);
		nodeTable.createListColumn("Biological_processes_hidden", String.class, false);
		nodeTable.createListColumn("Molecular_functions_hidden", String.class, false);
		nodeTable.createListColumn("Cellular_components", String.class, false);
		nodeTable.createListColumn("Biological_processes", String.class, false);
		nodeTable.createListColumn("Molecular_functions", String.class, false);
		nodeTable.createColumn("Queried", String.class, false);
		nodeTable.createListColumn("Orthologs", String.class, false);
		nodeTable.createColumn("Legend", String.class, false);
	}

	private void createEdges(CyNetwork network) {
		// Edge attributes
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createColumn("Reference_organism_interactor_A", String.class, false);
		edgeTable.createColumn("Reference_organism_interactor_B", String.class, false);
		edgeTable.createColumn("Interactor_A", String.class, false);
		edgeTable.createColumn("Interactor_B", String.class, false);
		edgeTable.createColumn("Gene_name_A", String.class, false);
		edgeTable.createColumn("Gene_name_B", String.class, false);
		edgeTable.createColumn("Protein_name_A", String.class, false);
		edgeTable.createColumn("Protein_name_B", String.class, false);
		edgeTable.createListColumn("Source", String.class, false);
		edgeTable.createListColumn("Detmethod", String.class, false);
		edgeTable.createListColumn("Type", String.class, false);
		edgeTable.createListColumn("Interaction_id", String.class, false);
		edgeTable.createListColumn("Pubid", String.class, false);
		edgeTable.createListColumn("Confidence", String.class, false);
		edgeTable.createColumn("Tax_id", String.class, false);
		edgeTable.createColumn("Interolog", String.class, false);

		for (Organism organism : interactionsByOrg.keySet()) {
			final Map<String, UniProtEntry> interactorInOrganism =
					interactorPool.identifiersInOrganism(organism);

			boolean inRefOrg = organism.equals(networkQueryParameters.getReferenceOrganism());

			for (EncoreInteraction interaction : interactionsByOrg.get(organism)) {
				String nodeAName = interaction.getInteractorA("uniprotkb");
				String nodeBName = interaction.getInteractorB("uniprotkb");

				final UniProtEntry refOrgProtA = getEntryByIdentifier(interactorInOrganism, nodeAName);
				final UniProtEntry refOrgProtB = getEntryByIdentifier(interactorInOrganism, nodeBName);

				CyNode nodeA = getOrCreateNode(network, refOrgProtA);
				CyNode nodeB = getOrCreateNode(network, refOrgProtB);

				if (nodeA != null && nodeB != null) {
					CyEdge myEdge = network.addEdge(nodeA, nodeB, true);

					CyRow rowA = network.getRow(nodeA);
					CyRow rowB = network.getRow(nodeB);
					
				    CyRow edgeAttr = network.getRow(myEdge);
					edgeAttr.set("Reference_organism_interactor_A", refOrgProtA.getUniProtId());
					edgeAttr.set("Reference_organism_interactor_B", refOrgProtB.getUniProtId());
					edgeAttr.set("Interactor_A", nodeAName);
					edgeAttr.set("Interactor_B", nodeBName);
					edgeAttr.set("Gene_name_A", rowA.get("gene_name", String.class));
					edgeAttr.set("Gene_name_B", rowB.get("gene_name", String.class));
					edgeAttr.set("Protein_name_A", rowA.get("protein_name", String.class));
					edgeAttr.set("Protein_name_B", rowB.get("protein_name", String.class));
					edgeAttr.set("Source", PsicquicResultTranslator.convert(interaction.getSourceDatabases()));
					edgeAttr.set("Detmethod", OLSClient.getInstance().convert(interaction.getMethodToPubmed().keySet()));
					edgeAttr.set("Type", OLSClient.getInstance().convert(interaction.getTypeToPubmed().keySet()));
					//edgeAttr.set("interaction_id", PsicquicResultTranslator.convert(interaction.getId()));
					edgeAttr.set("Pubid", PsicquicResultTranslator.convert(interaction.getPublicationIds()));
					edgeAttr.set("Confidence", PsicquicResultTranslator.convert(interaction.getConfidenceValues()));
					edgeAttr.set("Tax_id", String.valueOf(organism.getTaxId()));
					edgeAttr.set("Interolog", Boolean.toString(!inRefOrg));
					
				} else
					System.out.println("node not found with : " + nodeAName + (nodeA == null ? "[null]" : "") + " <-> " + nodeBName + (nodeB == null ? "[null]" : ""));
			}
		}
	}

	private UniProtEntry getEntryByIdentifier(Map<String, UniProtEntry> entries, String identifier) {
		final UniProtEntry entry = entries.get(identifier);
		if(entry == null) {
			return entries.get(ProteinUtils.UniProtId.extractStrictUniProtId(identifier));
		}
		return entry;
	}

	private CyNode getNode(UniProtEntry entry) {
		return nodeNameMap.get(entry);
	}

	private CyNode getOrCreateNode(CyNetwork network, UniProtEntry entry) {
		CyNode node = getNode(entry);
		if (node == null) {
			node = network.addNode();
			nodeNameMap.put(entry, node);

			final GeneOntologyTermSet geneOntologyTerms = entry.getGeneOntologyTerms();
			final GeneOntologyTermSet cellularComponent =
					geneOntologyTerms.getByCategory(GeneOntologyCategory.CELLULAR_COMPONENT);
			final GeneOntologyTermSet biologicalProcess =
					geneOntologyTerms.getByCategory(GeneOntologyCategory.BIOLOGICAL_PROCESS);
			final GeneOntologyTermSet molecularFunction =
					geneOntologyTerms.getByCategory(GeneOntologyCategory.MOLECULAR_FUNCTION);

			CyRow nodeAttr = network.getRow(node);
			nodeAttr.set("name", entry.getUniProtId());
			nodeAttr.set("Uniprot_id", entry.getUniProtId());
			nodeAttr.set("Accessions", new ArrayList<String>(entry.getAccessions()));
			// TODO: if gene_name is empty, put "[ptn]proteinName"
			nodeAttr.set("Gene_name", entry.getGeneName());
			nodeAttr.set("Ec_number", entry.getEcNumber());
			nodeAttr.set("Synonym_gene_names", new ArrayList<String>(entry.getSynonymGeneNames()));
			nodeAttr.set("Protein_name", entry.getProteinName());
			nodeAttr.set("Tax_id", String.valueOf(entry.getOrganism().getTaxId()));
			nodeAttr.set("Reviewed", String.valueOf(entry.isReviewed()));
			nodeAttr.set("Cellular_components_hidden",
					JSONUtils.jsonListToStringList(cellularComponent)
			);
			nodeAttr.set("Cellular_components", cellularComponent.asStringList());
			nodeAttr.set("Biological_processes_hidden",
					JSONUtils.jsonListToStringList(biologicalProcess)
			);
			nodeAttr.set("Biological_processes", biologicalProcess.asStringList());
			nodeAttr.set("Molecular_functions_hidden",
					JSONUtils.jsonListToStringList(molecularFunction)
			);
			nodeAttr.set("Molecular_functions", molecularFunction.asStringList());
			boolean queried = false;
			if(!networkQueryParameters.isInteractomeQuery()) {
				nodeAttr.set("Orthologs",
						JSONUtils.jsonListToStringList(interactorPool.getOrthologs(entry))
				);
				queried = proteinOfInterestPool.contains(entry);
			}
			nodeAttr.set("Queried", String.valueOf(queried));
			nodeAttr.set("Legend", "false");
		}

		return node;
	}

	private CyNetworkView applyView(CyNetwork network) {
		if (network == null) {
			return null;
		}
		this.networkManager.addNetwork(network);

		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if (views.size() != 0) {
			view = views.iterator().next();
		}

		if (view == null) {
			// create a new view for my network
			view = networkViewFactory.createNetworkView(network);
			networkViewManager.addNetworkView(view);
		} else {
			System.out.println("networkView already existed.");
		}

		return view;
	}

	private void applyLayout(CyNetworkView view, int numberOfNodes) {
		CyLayoutAlgorithm layout = null;
		if(numberOfNodes <= 2000) {
			layout = layoutAlgorithmManager.getLayout("force-directed");
		} else {
			layout = layoutAlgorithmManager.getLayout("grid");
		}
		Object context = layout.createLayoutContext();
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(
				layout.createTaskIterator(
						view,
						context,
						CyLayoutAlgorithm.ALL_NODE_VIEWS,
						layoutAttribute
				)
		);
	}

	private void applyVisualStyle(CyNetworkView view) {
		for (VisualStyle curVS : visualMappingManager.getAllVisualStyles()) {
			if (curVS.getTitle().equalsIgnoreCase("PPiMapBuilder Visual Style")) {
				curVS.apply(view);
				visualMappingManager.setCurrentVisualStyle(curVS);
				break;
			}
		}

		view.updateView();
	}

}

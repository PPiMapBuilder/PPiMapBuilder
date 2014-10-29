package ch.picard.ppimapbuilder.networkbuilder.network;

import ch.picard.ppimapbuilder.PMBActivator;
import ch.picard.ppimapbuilder.data.JSONUtils;
import ch.picard.ppimapbuilder.data.JSONable;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicResultTranslator;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyCategory;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import org.cytoscape.model.*;
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
import java.util.Map;

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


	private final NetworkQueryParameters networkQueryParameters;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;
	private final UniProtEntrySet proteinOfInterestPool;

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
			final UniProtEntrySet proteinOfInterestPool,
			final NetworkQueryParameters networkQueryParameters,
			long startTime
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

		this.nodeNameMap = new HashMap<UniProtEntry, CyNode>();

		this.startTime = startTime;
	}

	@Override
	public void run(TaskMonitor monitor) {
		monitor.setTitle("PPiMapBuilder interaction network build");
		monitor.setStatusMessage("Building Cytoscape network...");

		if (!interactionsByOrg.get(networkQueryParameters.getReferenceOrganism()).isEmpty() && !interactionsByOrg.isEmpty()) {

			// Create an empty network
			CyNetwork network = networkFactory.createNetwork();
			network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle("PPiMapBuilder network"));

			CyTable networkTable = network.getDefaultNetworkTable();
			networkTable.createColumn("created by", String.class, true);
			networkTable.createColumn("reference organism", String.class, true);
			networkTable.createListColumn("other organisms", String.class, true);
			networkTable.createListColumn("source databases", String.class, true);
			networkTable.createColumn("build time (seconds)", Integer.class, true);

			network.getRow(network).set(
					"created by",
					"PPiMapBuilder v"
							+ PMBActivator.version
							+ (PMBActivator.isSnapshot ? " " + PMBActivator.buildTimestamp : "") //display PMB build timestamp in snapshots
			);
			network.getRow(network).set("reference organism", networkQueryParameters.getReferenceOrganism().getScientificName());
			network.getRow(network).set("other organisms", OrganismUtils.organismsToStrings(networkQueryParameters.getOtherOrganisms()));
			network.getRow(network).set("source databases", InteractionUtils.psicquicServicesToStrings(networkQueryParameters.getSelectedDatabases()));
			monitor.setProgress(0.33);

			//Create nodes using interactors pool
			createNodes(network);
			monitor.setProgress(0.66);

			//Create edges using reference interactions and organism interactions
			createEdges(network);
			monitor.setProgress(0.99);

			// Creation on the view
			CyNetworkView view = applyView(network);

			// Layout
			applyLayout(view);

			// Visual Style
			applyVisualStyle(view);

			// Add network build time
			network.getRow(network).set(
					"build time (seconds)",
					(((int) (System.currentTimeMillis() - startTime)) / 1000)
			);
		}
		monitor.setProgress(1.0);
	}

	private void createNodes(CyNetwork network) {

		// Node attributes
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn("uniprot_id", String.class, false);
		nodeTable.createListColumn("accessions", String.class, false);
		nodeTable.createColumn("gene_name", String.class, false);
		nodeTable.createColumn("ec_number", String.class, false);
		nodeTable.createListColumn("synonym_gene_names", String.class, false);
		nodeTable.createColumn("protein_name", String.class, false);
		nodeTable.createColumn("tax_id", String.class, false);
		nodeTable.createColumn("reviewed", String.class, false);
		nodeTable.createListColumn("cellular_components_hidden", String.class, false);
		nodeTable.createListColumn("biological_processes_hidden", String.class, false);
		nodeTable.createListColumn("molecular_functions_hidden", String.class, false);
		nodeTable.createListColumn("orthologs", String.class, false);
		nodeTable.createListColumn("cellular_components", String.class, false);
		nodeTable.createListColumn("biological_processes", String.class, false);
		nodeTable.createListColumn("molecular_functions", String.class, false);
		nodeTable.createColumn("queried", String.class, false);

		for (UniProtEntry protein : interactorPool) {
			if (!nodeNameMap.containsKey(protein)) {
				CyNode node = network.addNode();
				nodeNameMap.put(protein, node);

				final GeneOntologyTermSet geneOntologyTerms = protein.getGeneOntologyTerms();
				final GeneOntologyTermSet cellularComponent =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.CELLULAR_COMPONENT);
				final GeneOntologyTermSet biologicalProcess =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.BIOLOGICAL_PROCESS);
				final GeneOntologyTermSet molecularFunction =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.MOLECULAR_FUNCTION);

				CyRow nodeAttr = network.getRow(node);
				nodeAttr.set("name", protein.getUniProtId());
				nodeAttr.set("uniprot_id", protein.getUniProtId());
				nodeAttr.set("accessions", new ArrayList<String>(protein.getAccessions()));
				nodeAttr.set("gene_name", protein.getGeneName());
				nodeAttr.set("ec_number", protein.getEcNumber());
				nodeAttr.set("synonym_gene_names", new ArrayList<String>(protein.getSynonymGeneNames()));
				nodeAttr.set("protein_name", protein.getProteinName());
				nodeAttr.set("tax_id", String.valueOf(protein.getOrganism().getTaxId()));
				nodeAttr.set("reviewed", String.valueOf(protein.isReviewed()));
				nodeAttr.set("cellular_components_hidden",
						JSONUtils.jsonListToStringList(
								cellularComponent.toArray(
										new JSONable[cellularComponent.size()]
								)
						)
				);
				nodeAttr.set("cellular_components", cellularComponent.asStringList());
				nodeAttr.set("biological_processes_hidden",
						JSONUtils.jsonListToStringList(
								biologicalProcess.toArray(
										new JSONable[biologicalProcess.size()]
								)
						)
				);
				nodeAttr.set("biological_processes", biologicalProcess.asStringList());
				nodeAttr.set("molecular_functions_hidden",
						JSONUtils.jsonListToStringList(
								molecularFunction.toArray(
										new JSONable[molecularFunction.size()]
								)
						)
				);
				nodeAttr.set("molecular_functions", molecularFunction.asStringList());
				nodeAttr.set("orthologs",
						JSONUtils.jsonListToStringList(
								protein.getOrthologs().toArray(
										new JSONable[protein.getOrthologs().size()]
								)
						)
				);
				nodeAttr.set("queried", String.valueOf(proteinOfInterestPool.contains(protein)));
			}
		}
	}

	private void createEdges(CyNetwork network) {
		// Edge attributes
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createColumn("Interactor_A", String.class, false);
		edgeTable.createColumn("Interactor_B", String.class, false);
		edgeTable.createColumn("Gene_name_A", String.class, false);
		edgeTable.createColumn("Gene_name_B", String.class, false);
		edgeTable.createColumn("Protein_name_A", String.class, false);
		edgeTable.createColumn("Protein_name_B", String.class, false);
		edgeTable.createListColumn("source", String.class, false);
		edgeTable.createListColumn("detmethod", String.class, false);
		edgeTable.createListColumn("type", String.class, false);
		edgeTable.createListColumn("interaction_id", String.class, false);
		edgeTable.createListColumn("pubid", String.class, false);
		edgeTable.createListColumn("confidence", String.class, false);
		edgeTable.createColumn("tax_id", String.class, false);
		edgeTable.createColumn("interolog", String.class, false);

		for (Organism organism : interactionsByOrg.keySet()) {

			boolean inRefOrg = organism.equals(networkQueryParameters.getReferenceOrganism());
			for (EncoreInteraction interaction : interactionsByOrg.get(organism)) {
				CyNode nodeA = null, nodeB = null;

				String nodeAName = interaction.getInteractorA("uniprotkb");
				String nodeBName = interaction.getInteractorB("uniprotkb");

				if (inRefOrg) {
					nodeA = getNodeFromUniProtId(nodeAName);
					nodeB = getNodeFromUniProtId(nodeBName);
				} else {
					Map<Protein, UniProtEntry> proteinInOrganismWithReferenceEntry = interactorPool.findProteinInOrganismWithReferenceEntry(organism);
					for (Protein ortholog : proteinInOrganismWithReferenceEntry.keySet()) {
						String uniProtId = proteinInOrganismWithReferenceEntry.get(ortholog).getUniProtId();
						if (ortholog.getUniProtId().equals(nodeAName)) {
							nodeA = getNodeFromUniProtId(uniProtId);
						}
						if (ortholog.getUniProtId().equals(nodeBName)) {
							nodeB = getNodeFromUniProtId(uniProtId);
						}
						if (nodeA != null && nodeB != null) break;
					}
				}

				if (nodeA != null && nodeB != null) {
					CyEdge myEdge = network.addEdge(nodeA, nodeB, true);

					CyRow rowA = network.getRow(nodeA);
					CyRow rowB = network.getRow(nodeB);

					CyRow edgeAttr = network.getRow(myEdge);
					edgeAttr.set("Interactor_A", nodeAName);
					edgeAttr.set("Interactor_B", nodeBName);
					edgeAttr.set("Gene_name_A", rowA.get("gene_name", String.class));
					edgeAttr.set("Gene_name_B", rowB.get("gene_name", String.class));
					edgeAttr.set("Protein_name_A", rowA.get("protein_name", String.class));
					edgeAttr.set("Protein_name_B", rowB.get("protein_name", String.class));
					edgeAttr.set("source", PsicquicResultTranslator.convert(interaction.getSourceDatabases()));
					edgeAttr.set("detmethod", PsicquicResultTranslator.convert(interaction.getMethodToPubmed().keySet()));
					edgeAttr.set("type", PsicquicResultTranslator.convert(interaction.getTypeToPubmed().keySet()));
					//edgeAttr.set("interaction_id", PsicquicResultTranslator.convert(interaction.getId()));
					edgeAttr.set("pubid", PsicquicResultTranslator.convert(interaction.getPublicationIds()));
					edgeAttr.set("confidence", PsicquicResultTranslator.convert(interaction.getConfidenceValues()));
					edgeAttr.set("tax_id", String.valueOf(organism.getTaxId()));
					edgeAttr.set("interolog", Boolean.toString(!inRefOrg));
				}
				else System.out.println("node not found with : "+nodeAName + " -> " + nodeBName);
			}
		}
	}

	private CyNode getNodeFromUniProtId(String uniProtId) {
		return nodeNameMap.get(
				interactorPool.find(uniProtId)
		);
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

	private void applyLayout(CyNetworkView view) {
		CyLayoutAlgorithm layout = layoutAlgorithmManager.getLayout("force-directed");
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

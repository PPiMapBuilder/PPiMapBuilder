package tk.nomis_tech.ppimapbuilder.networkbuilder.network;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

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

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import tk.nomis_tech.ppimapbuilder.data.UniProtProtein;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicResultTranslator;
import tk.nomis_tech.ppimapbuilder.webservice.UniProtEntryClient;

public class PMBCreateNetworkTask extends AbstractTask {

	// For the network
	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;

	// For the view
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;

	// For the layout
	private final CyLayoutAlgorithmManager layoutManager;

	// For the visual style
	private final VisualMappingManager vmm;

	private final Collection<BinaryInteraction> interactionResults;

	public PMBCreateNetworkTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil, final CyNetworkFactory cnf, CyNetworkViewFactory cnvf,
			final CyNetworkViewManager networkViewManager, final CyLayoutAlgorithmManager layoutMan, final VisualMappingManager vmm,
			Collection<BinaryInteraction> interactionResults) {
		// For the network
		this.netMgr = netMgr;
		this.cnf = cnf;
		this.namingUtil = namingUtil;

		// For the view
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;

		// For the layout
		this.layoutManager = layoutMan;

		// For the visual style
		this.vmm = vmm;

		this.interactionResults = interactionResults;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		if (!interactionResults.isEmpty()) {
			createNetworkFromBinaryInteractions(interactionResults);
		}
	}

	public void createNetworkFromBinaryInteractions(Collection<BinaryInteraction> binaryInteractions) {
		// Create an empty network
		CyNetwork myNet = cnf.createNetwork();
		myNet.getRow(myNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("PPiMapBuilder network"));
		CyTable netAttr = myNet.getDefaultNetworkTable();
		netAttr.createColumn("created by", String.class, true);
		myNet.getRow(myNet).set("created by", "PPiMapBuilder");

		// Edge attributes
		CyTable edgeAttr = myNet.getDefaultEdgeTable();
		edgeAttr.createListColumn("source", String.class, false);
		edgeAttr.createListColumn("detmethod", String.class, false);
		edgeAttr.createListColumn("type", String.class, false);
		edgeAttr.createListColumn("interaction_id", String.class, false);
		edgeAttr.createListColumn("pubid", String.class, false);
		edgeAttr.createListColumn("confidence", String.class, false);

		// Node attributes
		CyTable nodeAttr = myNet.getDefaultNodeTable();
		nodeAttr.createColumn("uniprot_id", String.class, false);
		nodeAttr.createColumn("gene_name", String.class, false);
		nodeAttr.createColumn("ec_number", String.class, false);
		nodeAttr.createListColumn("synonym_gene_names", String.class, false);
		nodeAttr.createColumn("protein_name", String.class, false);
		nodeAttr.createColumn("tax_id", String.class, false);
		nodeAttr.createColumn("reviewed", String.class, false);
		nodeAttr.createListColumn("cellular_components", String.class, false);
		nodeAttr.createListColumn("biological_processes", String.class, false);
		nodeAttr.createListColumn("molecular_functions", String.class, false);

		// Add nodes
		HashMap<String, CyNode> nodeNameMap = new HashMap<String, CyNode>();

		for (BinaryInteraction interaction : binaryInteractions) { // For each
																	// interaction

			// System.out.println(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier()+"\t"+interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
			// TODO : treat cases without uniprotkb id

			// Retrieve the first node name
			CyNode node1 = null;
			String name1 = null;
			for (CrossReference cr : interaction.getInteractorA().getIdentifiers()) {
				if (cr.getDatabase().equals("uniprotkb")) {
					name1 = cr.getIdentifier();
					break;
				}
			}
			if (name1 == null) {
				continue;
			}
			// Retrieve the second node name
			CyNode node2 = null;
			String name2 = null;
			for (CrossReference cr : interaction.getInteractorB().getIdentifiers()) {
				if (cr.getDatabase().equals("uniprotkb")) {
					name2 = cr.getIdentifier();
					break;
				}
			}
			if (name2 == null) {
				continue;
			}

			// Retrieve or create the first node
			if (nodeNameMap.containsKey(name1)) {
				node1 = nodeNameMap.get(name1);
			} else {
				node1 = myNet.addNode();
				CyRow attributes = myNet.getRow(node1);
				attributes.set("name", name1);
				nodeNameMap.put(name1, node1);

				// Add attributes to first node
				try {
					UniProtProtein prot = UniProtEntryClient.getInstance().retrieveProteinData(name1);
					CyRow attributesNode1 = myNet.getRow(node1);
					attributesNode1.set("uniprot_id", prot.getUniprotId());
					attributesNode1.set("tax_id", String.valueOf(prot.getTaxId()));
					attributesNode1.set("gene_name", prot.getGeneName());
					attributesNode1.set("synonym_gene_names", prot.getSynonymGeneNames());
					attributesNode1.set("protein_name", prot.getProteinName());
					attributesNode1.set("ec_number", prot.getEcNumber());
					attributesNode1.set("reviewed", String.valueOf(prot.isReviewed()));
					attributesNode1.set("cellular_components", prot.getCellularComponentsAsStringList());
					attributesNode1.set("biological_processes", prot.getBiologicalProcessesAsStringList());
					attributesNode1.set("molecular_functions", prot.getMolecularFunctionsAsStringList());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Retrieve or create the second node
			if (nodeNameMap.containsKey(name2)) {
				node2 = nodeNameMap.get(name2);
			} else {
				node2 = myNet.addNode();
				CyRow attributes = myNet.getRow(node2);
				attributes.set("name", name2);
				nodeNameMap.put(name2, node2);

				// Add attributes to second node
				try {
					UniProtProtein prot = UniProtEntryClient.getInstance().retrieveProteinData(name2);
					CyRow attributesNode2 = myNet.getRow(node2);
					attributesNode2.set("uniprot_id", prot.getUniprotId());
					attributesNode2.set("tax_id", String.valueOf(prot.getTaxId()));
					attributesNode2.set("gene_name", prot.getGeneName());
					attributesNode2.set("synonym_gene_names", prot.getSynonymGeneNames());
					attributesNode2.set("protein_name", prot.getProteinName());
					attributesNode2.set("ec_number", prot.getEcNumber());
					attributesNode2.set("reviewed", String.valueOf(prot.isReviewed()));
					attributesNode2.set("cellular_components", prot.getCellularComponentsAsStringList());
					attributesNode2.set("biological_processes", prot.getBiologicalProcessesAsStringList());
					attributesNode2.set("molecular_functions", prot.getMolecularFunctionsAsStringList());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			

			// Add edges & attributes
			CyEdge myEdge = myNet.addEdge(node1, node2, true);
			CyRow attributes = myNet.getRow(myEdge);
			attributes.set("source", PsicquicResultTranslator.convert(interaction.getSourceDatabases()));
			attributes.set("detmethod", PsicquicResultTranslator.convert(interaction.getDetectionMethods()));
			attributes.set("type", PsicquicResultTranslator.convert(interaction.getInteractionTypes()));
			attributes.set("interaction_id", PsicquicResultTranslator.convert(interaction.getInteractionAcs()));
			attributes.set("pubid", PsicquicResultTranslator.convert(interaction.getPublications()));
			attributes.set("confidence", PsicquicResultTranslator.convert(interaction.getConfidenceValues()));

		}

		// Creation on the view
		CyNetworkView myView = applyView(myNet);

		// Layout
		applyLayout(myView);

		// Visual Style
		applyVisualStyle(myView);

		// System.out.println("Done !");
	}

	public CyNetworkView applyView(CyNetwork myNet) {
		if (myNet == null) {
			return null;
		}
		this.netMgr.addNetwork(myNet);

		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNet);
		CyNetworkView myView = null;
		if (views.size() != 0) {
			myView = views.iterator().next();
		}

		if (myView == null) {
			// create a new view for my network
			myView = cnvf.createNetworkView(myNet);
			networkViewManager.addNetworkView(myView);
		} else {
			System.out.println("networkView already existed.");
		}

		return myView;
	}

	public void applyLayout(CyNetworkView myView) {
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		Object context = layout.createLayoutContext();
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(layout.createTaskIterator(myView, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
	}

	public void applyVisualStyle(CyNetworkView myView) {
		VisualStyle vs = vmm.getDefaultVisualStyle();
		vs.apply(myView);
		myView.updateView();
	}

}

package tk.nomis_tech.ppimapbuilder.networkbuilder.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.eclipsesource.json.JsonObject;

import tk.nomis_tech.ppimapbuilder.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntryCollection;
import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicResultTranslator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBCreateNetworkTask extends AbstractTask {

	// For the network
	private final CyNetworkManager networkManager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkNaming namingUtil;

	// For the view
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkViewManager viewManager;

	// For the layout
	private final CyLayoutAlgorithmManager layoutManager;

	// For the visual style
	private final VisualMappingManager vizMapManager;


	private final Organism refOrg;
	private final HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntryCollection interactorPool;
	private final PMBInteractionNetworkBuildTaskFactory pmbInteractionNetworkBuildTaskFactory;
	
	//Network data
	private final HashMap<String, CyNode> nodeNameMap;

	public PMBCreateNetworkTask(PMBInteractionNetworkBuildTaskFactory pmbInteractionNetworkBuildTaskFactory, final CyNetworkManager netMgr, final CyNetworkNaming namingUtil, final CyNetworkFactory cnf, CyNetworkViewFactory cnvf,
			final CyNetworkViewManager networkViewManager, final CyLayoutAlgorithmManager layoutMan, final VisualMappingManager vmm,
			HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg, UniProtEntryCollection interactorPool, QueryWindow queryWindow) {
		this.pmbInteractionNetworkBuildTaskFactory = pmbInteractionNetworkBuildTaskFactory;
		
		// For the network
		this.networkManager = netMgr;
		this.networkFactory = cnf;
		this.namingUtil = namingUtil;

		// For the view
		this.viewFactory = cnvf;
		this.viewManager = networkViewManager;

		// For the layout
		this.layoutManager = layoutMan;

		// For the visual style
		this.vizMapManager = vmm;

		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.refOrg = queryWindow.getSelectedRefOrganism();
		
		this.nodeNameMap = new HashMap<String, CyNode>();
	}

	@Override
	public void run(TaskMonitor monitor) {
		
		monitor.setTitle("Network building");
		monitor.setStatusMessage("Building Cytoscape network...");
		monitor.setProgress(1.0);
		
		if (!interactionsByOrg.get(refOrg.getTaxId()).isEmpty() && !interactionsByOrg.isEmpty()) {
			
			// Create an empty network
			CyNetwork network = networkFactory.createNetwork();
			network.getRow(network).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("PPiMapBuilder network"));
			
			CyTable networkTable = network.getDefaultNetworkTable();
			networkTable.createColumn("created by", String.class, true);
			network.getRow(network).set("created by", "PPiMapBuilder");
			
			//Create nodes using interactors pool
			createNodes(network);
			
			//Create edges using reference interactions and ortholog interactions
			createEdges(network);
			
			// Creation on the view
			CyNetworkView view = applyView(network);

			// Layout
			applyLayout(view);

			// Visual Style
			applyVisualStyle(view);
		}
	}
	
	private void createNodes(CyNetwork network) {
		// Node attributes
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn("uniprot_id", String.class, false);
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
				
		for (UniProtEntry protein : interactorPool) {
			if (!nodeNameMap.containsKey(protein.getUniprotId())) {
				CyNode node = network.addNode();
				nodeNameMap.put(protein.getUniprotId(), node);
				
				CyRow nodeAttr = network.getRow(node);
				nodeAttr.set("name", protein.getUniprotId());
				nodeAttr.set("uniprot_id", protein.getUniprotId());
				nodeAttr.set("gene_name", protein.getGeneName());
				nodeAttr.set("ec_number", protein.getEcNumber());
				nodeAttr.set("synonym_gene_names", protein.getSynonymGeneNames());
				nodeAttr.set("protein_name", protein.getProteinName());
				nodeAttr.set("tax_id", String.valueOf(protein.getTaxId()));
				nodeAttr.set("reviewed", String.valueOf(protein.isReviewed()));
				nodeAttr.set("cellular_components_hidden", protein.getCellularComponentsAsStringList());
				nodeAttr.set("biological_processes_hidden", protein.getBiologicalProcessesAsStringList());
				nodeAttr.set("molecular_functions_hidden", protein.getMolecularFunctionsAsStringList());
				nodeAttr.set("orthologs", protein.getOrthologsAsStringList());
				
				{
					List<String> cellularComponent = protein.getCellularComponentsAsStringList();
					List<String> cellularComponentReadable = new ArrayList<String>();
					if (cellularComponent != null && !cellularComponent.isEmpty())
						for (String s : cellularComponent) {
							JsonObject obj = JsonObject.readFrom(s);
							cellularComponentReadable.add(obj.get("term").asString());
						}
					nodeAttr.set("cellular_components", cellularComponentReadable);
					
					List<String> biologicalProcess = protein.getBiologicalProcessesAsStringList();
					List<String> biologicalProcessReadable = new ArrayList<String>();
					if (biologicalProcess != null && !biologicalProcess.isEmpty())
						for (String s : biologicalProcess) {
							JsonObject obj = JsonObject.readFrom(s);
							biologicalProcessReadable.add(obj.get("term").asString());
						}
					nodeAttr.set("biological_processes", biologicalProcessReadable);
					
					List<String> molecularFunction = protein.getMolecularFunctionsAsStringList();
					List<String> molecularFunctionReadable = new ArrayList<String>();
					if (molecularFunction != null && !molecularFunction.isEmpty())
						for (String s : molecularFunction) {
							JsonObject obj = JsonObject.readFrom(s);
							molecularFunctionReadable.add(obj.get("term").asString());
						}
					nodeAttr.set("molecular_functions", molecularFunctionReadable);
					
					
				}
				
				
				
			}
		}
	}

	private void createEdges(CyNetwork network) {
		// Edge attributes
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createListColumn("source", String.class, false);
		edgeTable.createListColumn("detmethod", String.class, false);
		edgeTable.createListColumn("type", String.class, false);
		edgeTable.createListColumn("interaction_id", String.class, false);
		edgeTable.createListColumn("pubid", String.class, false);
		edgeTable.createListColumn("confidence", String.class, false);
		edgeTable.createColumn("tax_id", String.class, false);
		edgeTable.createColumn("interolog", String.class, false);
		
		for(Integer taxId: interactionsByOrg.keySet()) {
			boolean inRefOrg = taxId == refOrg.getTaxId();
			for(EncoreInteraction interaction: interactionsByOrg.get(taxId)) {
				CyNode nodeA = null, nodeB = null;
				if(inRefOrg) {
					nodeA = nodeNameMap.get(interaction.getInteractorA("uniprotkb"));
					nodeB = nodeNameMap.get(interaction.getInteractorB("uniprotkb"));					
				}
				else {
					for(UniProtEntry prot: interactorPool) {
						OrthologProtein ortho = prot.getOrthologByTaxid(taxId);
						if(ortho != null) {
							if(interaction.getInteractorA().equals(ortho.getUniprotId())) 
								nodeA = nodeNameMap.get(prot.getUniprotId());
							if(interaction.getInteractorB().equals(ortho.getUniprotId())) 
								nodeB = nodeNameMap.get(prot.getUniprotId());
						}
						
						if(nodeA != null && nodeB != null) break;
					}
				}
				
				if(nodeA != null && nodeB != null) {				
					CyEdge myEdge = network.addEdge(nodeA, nodeB, true);
					
					CyRow edgeAttr = network.getRow(myEdge);
					edgeAttr.set("source", PsicquicResultTranslator.convert(interaction.getSourceDatabases()));
					edgeAttr.set("detmethod", PsicquicResultTranslator.convert(interaction.getMethodToPubmed().keySet()));
					edgeAttr.set("type", PsicquicResultTranslator.convert(interaction.getTypeToPubmed().keySet()));
					//edgeAttr.set("interaction_id", PsicquicResultTranslator.convert(interaction.getId()));
					edgeAttr.set("pubid", PsicquicResultTranslator.convert(interaction.getPublicationIds()));
					edgeAttr.set("confidence", PsicquicResultTranslator.convert(interaction.getConfidenceValues()));
					edgeAttr.set("tax_id", taxId.toString());
					edgeAttr.set("interolog", Boolean.toString(!inRefOrg));
				}
			}
		}
	}

	private CyNetworkView applyView(CyNetwork network) {
		if (network == null) {
			return null;
		}
		this.networkManager.addNetwork(network);

		final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if (views.size() != 0) {
			view = views.iterator().next();
		}

		if (view == null) {
			// create a new view for my network
			view = viewFactory.createNetworkView(network);
			viewManager.addNetworkView(view);
		} else {
			System.out.println("networkView already existed.");
		}

		return view;
	}

	private void applyLayout(CyNetworkView view) {
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		Object context = layout.createLayoutContext();
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
	}

	private void applyVisualStyle(CyNetworkView view) {
		Iterator it = vizMapManager.getAllVisualStyles().iterator();
		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (curVS.getTitle().equalsIgnoreCase("PPiMapBuilder Visual Style"))
			{
				curVS.apply(view);
				vizMapManager.setCurrentVisualStyle(curVS);
				break;
			}
		}
		
		view.updateView();
	}
	
	public PMBInteractionNetworkBuildTaskFactory getPmbInteractionNetworkBuildTaskFactory() {
		return pmbInteractionNetworkBuildTaskFactory;
	}

}

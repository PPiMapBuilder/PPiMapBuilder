package tk.nomis_tech.ppimapbuilder.networkbuilder.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import psidev.psi.mi.tab.model.Author;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import tk.nomis_tech.ppimapbuilder.util.PsicquicResultTranslator;

public class PMBCreateNetworkTask extends AbstractTask {

	// For the network
	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;

	//For the view
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;

	// For the layout
	private final CyLayoutAlgorithmManager layoutManager;

	// For the visual style
	private final VisualMappingManager vmm;

	private final Collection<BinaryInteraction> interactionResults;

	public PMBCreateNetworkTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil, final CyNetworkFactory cnf,
		CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager, final CyLayoutAlgorithmManager layoutMan, final VisualMappingManager vmm, Collection<BinaryInteraction> interactionResults) {
		// For the network
		this.netMgr = netMgr;
		this.cnf = cnf;
		this.namingUtil = namingUtil;

		//For the view
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
		myNet.getRow(myNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("My Network"));
		CyTable edgeAttr = myNet.getDefaultEdgeTable();

		edgeAttr.createColumn("BLUPBLUP", String.class, false);
		edgeAttr.createColumn("TiTi", String.class, false);
		edgeAttr.createListColumn("database.source", String.class, false);
		edgeAttr.createListColumn("authors", String.class, false);
		edgeAttr.createListColumn("det.method", String.class, false);

		// Add nodes        
		HashMap<String, CyNode> nodeNameMap = new HashMap<String, CyNode>();

		for (BinaryInteraction interaction : binaryInteractions) { // For each interaction

			//System.out.println(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier()+"\t"+interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
			// TODO : treat cases without uniprotkb id	
			// Retrieve or create the first node
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

			if (nodeNameMap.containsKey(name1)) {
				node1 = nodeNameMap.get(name1);
			} else {
				node1 = myNet.addNode();
				CyRow attributes = myNet.getRow(node1);
				attributes.set("name", name1);
				nodeNameMap.put(name1, node1);
			}
			// Retrieve or create the second node
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

			if (nodeNameMap.containsKey(name2)) {
				node2 = nodeNameMap.get(name2);
			} else {
				node2 = myNet.addNode();
				CyRow attributes = myNet.getRow(node2);
				attributes.set("name", name2);
				nodeNameMap.put(name2, node2);
			}

			// Add edges & attributes
			CyEdge myEdge = myNet.addEdge(node1, node2, true);
			CyRow attributes = myNet.getRow(myEdge);

			attributes.set("database.source", PsicquicResultTranslator.convert(interaction.getSourceDatabases()));
			attributes.set("authors", PsicquicResultTranslator.convert(interaction.getAuthors()));
			attributes.set("det.method", PsicquicResultTranslator.convert(interaction.getDetectionMethods()));

		}

		//Creation on the view
		CyNetworkView myView = applyView(myNet);

		// Layout
		applyLayout(myView);

		// Visual Style
		applyVisualStyle(myView);

		//System.out.println("Done !");
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

//	private void addEdgeData(BinaryInteraction interaction) {
//		// Step 1: create a new table
//		CyTable table = tableFactory.createTable("MyTable " + Integer.toString(numImports++), 
//				   "name", String.class, true, true);
//
//		// create a column for the table
//		String columnName = "MyColumn"; 
//		table.createColumn(columnName, Integer.class, false);
//		
//		// Step 2: populate the table with some data
//		String[] keys = {"YLL021W","YBR170C","YLR249W"}; //map to the the "name" column
//		CyRow row = table.getRow(keys[0]);
//		row.set(columnName, new Integer(2));
//
//		row = table.getRow(keys[1]);
//		row.set(columnName, new Integer(3));
//
//		row = table.getRow(keys[2]);
//		row.set(columnName, new Integer(4));
//
//		// Step 3: pass the new table to MapTableToNetworkTablesTaskFactory
//		super.insertTasksAfterCurrentTask( mapTableToNetworkTablesTaskFactory.createTaskIterator(table) );
//	}
}

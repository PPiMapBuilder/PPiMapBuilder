package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBGOSlimLayoutTask extends AbstractTask {

	private final CyNetworkView view;
	private final CyNetwork network;
	private final CyLayoutAlgorithmManager layoutManager;
	
	public PMBGOSlimLayoutTask(CyNetworkView view, CyLayoutAlgorithmManager layoutManager) {
		this.view = view;
		this.network = view.getModel();
		this.layoutManager = layoutManager;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		System.out.println("PMB Layout Task");
		
		// TODO : generate list of major GO in the network
		CyTable nodeTable = view.getModel().getDefaultNodeTable();
		
		for (CyNode n : network.getNodeList()) {
			System.out.print(network.getRow(n).get("uniprot_id", String.class)+ " -> ");
			for (String s : network.getRow(n).getList("go_slim", String.class)) {
				System.out.print(s+",");
			}
			System.out.println();
		}
		
		// TODO : assign one major GO for each prot
		
		
		// TODO : call attribute layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		Object context = layout.createLayoutContext();
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
	
	}

	
				

}

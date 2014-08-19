package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import java.util.ArrayList;

public class PMBGOSlimLayoutTaskFactory implements NetworkViewTaskFactory {
	private CyLayoutAlgorithmManager layoutManager;

	public PMBGOSlimLayoutTaskFactory(CyLayoutAlgorithmManager layoutManager) {
		this.layoutManager = layoutManager;
	}

	public TaskIterator createTaskIterator(CyNetworkView view) {
		System.out.println("PMBGOSlimLayoutTaskFactory");
		System.out.println(view);
		
		CyNetwork network = view.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		if (nodeTable.getColumn("go_slim") == null) {
			nodeTable.createListColumn("go_slim", String.class, false);
		}
		
		for (CyNode n : network.getNodeList()) {
			ArrayList<String> list = new ArrayList<String>();
			//list.add("toto");
			// TODO: do not reinit the go_slim before OK is pressed
			network.getRow(n).set("go_slim", list);
		}
		
		
		return new TaskIterator(
			new PMBGOSlimQueryTask(network),
			new PMBGOSlimLayoutTask(view, layoutManager)
		);
	}

	public boolean isReady(CyNetworkView view) {
		return view != null;
	};
}

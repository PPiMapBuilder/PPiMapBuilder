package ch.picard.ppimapbuilder.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

import ch.picard.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import ch.picard.ppimapbuilder.networkbuilder.query.PMBQueryInteractionTask;

public class PMBGOSlimLayoutTaskFactory implements NetworkViewTaskFactory {
	private CyLayoutAlgorithmManager layoutManager;

	public PMBGOSlimLayoutTaskFactory(CyLayoutAlgorithmManager layoutManager, CyNetworkManager cyNetworkManagerServiceRef) {
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
			list.add("toto");
			network.getRow(n).set("go_slim", list);
		}
		
		
		return new TaskIterator(
			new PMBGOSlimLayoutTask(view, layoutManager)
		);
	}

	public boolean isReady(CyNetworkView view) {
		return view != null;
	};
}

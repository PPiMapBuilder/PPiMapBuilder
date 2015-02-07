package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.*;

class PMBGOSlimLayoutTask extends AbstractTask {

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
		//System.out.println("PMB Layout Task");

		// Call attribute layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("attributes-layout");
		Object context = layout.createLayoutContext();
		String layoutAttribute = "Go_slim_group";
		insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));

	}

}

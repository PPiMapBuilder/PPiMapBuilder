package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class PMBGOSlimLayoutTaskFactory implements NetworkViewTaskFactory {
	private CyLayoutAlgorithmManager layoutManager;

	public PMBGOSlimLayoutTaskFactory(CyLayoutAlgorithmManager layoutManager) {
		this.layoutManager = layoutManager;
	}

	public TaskIterator createTaskIterator(CyNetworkView view) {
		//System.out.println("PMBGOSlimLayoutTaskFactory");
		//System.out.println(view);
		
		CyNetwork network = view.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		if (nodeTable.getColumn("Go_slim") == null) {
			nodeTable.createListColumn("Go_slim", String.class, false);
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

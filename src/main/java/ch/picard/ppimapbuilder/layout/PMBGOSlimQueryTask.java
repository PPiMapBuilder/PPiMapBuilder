package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBGOSlimQueryTask extends AbstractTask {

	private final CyNetwork network;

	public PMBGOSlimQueryTask(CyNetwork network) {
		this.network = network;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("Fetching slimmed Gene Ontology");
		System.out.println("PMB Layout Task");
		
		for (CyNode n : network.getNodeList()) {
			for (String s : network.getRow(n).getList("go_slim", String.class)) {
				System.out.print(s+",");
			}
			System.out.println();
		}
	}

	
				

}

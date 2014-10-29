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
		
		// Generate list of major GO in the network
		CyTable nodeTable = view.getModel().getDefaultNodeTable();
		
		ArrayList<String> fullListOfGOs = new ArrayList<String>(); // Stores every GO from the network
		for (CyNode n : network.getNodeList()) {
			List<String> go_slim = network.getRow(n).getList("go_slim", String.class);
			if(go_slim != null)
				fullListOfGOs.addAll(go_slim);
		}
		
		LinkedHashMap<String, Integer> goOccurrences = new LinkedHashMap<String, Integer>(); // Stores the number of occurrences for each GO
		for (String go : fullListOfGOs) {
			goOccurrences.put(go, Collections.frequency(fullListOfGOs, go));
		}

        ValueComparator bvc =  new ValueComparator(goOccurrences);
        TreeMap<String,Integer> sortedGoOccurrences = new TreeMap<String,Integer>(bvc); // Stores the GO ordered by amount
		sortedGoOccurrences.putAll(goOccurrences);
		System.out.println(sortedGoOccurrences);
        
		
		// Assign one major GO for each prot
		if (nodeTable.getColumn("go_slim_group") == null) { // Create node attribute to store the cluster assignement
			nodeTable.createColumn("go_slim_group", String.class, false);
		}
		for (CyNode n : network.getNodeList()) {
			List<String> tempGOList = network.getRow(n).getList("go_slim", String.class);
			if(tempGOList == null) continue;
			for (String key : sortedGoOccurrences.keySet()) { // For each GO beginning by the most frequent
				if (tempGOList.contains(key)) { // If this GO is one of those assigned to the current node 
					//System.out.print(key);
					network.getRow(n).set("go_slim_group", key); // We consider this GO as the major to cluster this node
					break;
				}
			}
		}
		
		
		// Call attribute layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("attributes-layout");
		Object context = layout.createLayoutContext();
		String layoutAttribute = "go_slim_group";
		insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
		
	}

	class ValueComparator implements Comparator<String> {

		LinkedHashMap<String, Integer> base;
		public ValueComparator(LinkedHashMap<String, Integer> base) {
			this.base = base;
		}

		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}


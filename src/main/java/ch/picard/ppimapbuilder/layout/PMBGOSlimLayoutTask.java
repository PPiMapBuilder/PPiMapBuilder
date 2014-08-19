package ch.picard.ppimapbuilder.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

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
		
		// Generate list of major GO in the network
		CyTable nodeTable = view.getModel().getDefaultNodeTable();
		
		ArrayList<String> fullListOfGOs = new ArrayList<String>(); // Stores every GO from the network
		for (CyNode n : network.getNodeList()) {
			//System.out.print(network.getRow(n).get("uniprot_id", String.class)+ " -> ");
			for (String s : network.getRow(n).getList("go_slim", String.class)) {
				fullListOfGOs.add(s);
			}
		}
		
		LinkedHashMap<String, Integer> goOccurrences = new LinkedHashMap<String, Integer>(); // Stores the number of occurrences for each GO
		for (String go : fullListOfGOs) {
			goOccurrences.put(go, Collections.frequency(fullListOfGOs, go));
		}
		//System.out.println(goOccurrences);

        ValueComparator bvc =  new ValueComparator(goOccurrences);
        TreeMap<String,Integer> sortedGoOccurrences = new TreeMap<String,Integer>(bvc); // Stores the GO ordered by amount
		sortedGoOccurrences.putAll(goOccurrences);
		System.out.println(sortedGoOccurrences);
        
		
		// TODO : assign one major GO for each prot
		
		
		// TODO : call attribute layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		Object context = layout.createLayoutContext();
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(layout.createTaskIterator(view, context, CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
	
	}

	
				

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

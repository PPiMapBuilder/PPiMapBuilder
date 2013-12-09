package tk.nomis_tech.ppimapbuilder.network;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBCreateNetworkTask extends AbstractTask{

	
    private final CyNetworkManager netMgr;
    private final CyNetworkFactory cnf;
    private final CyNetworkNaming namingUtil;
    
    //For the view
    private final CyNetworkViewFactory cnvf;
    private final CyNetworkViewManager networkViewManager;
    
    public PMBCreateNetworkTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil, final CyNetworkFactory cnf,
    			CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager){
            this.netMgr = netMgr;
            this.cnf = cnf;
            this.namingUtil = namingUtil;
            
            //For the view
            this.cnvf = cnvf;
            this.networkViewManager = networkViewManager;
    }
    
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Create an empty network
        CyNetwork myNet = cnf.createNetwork();
        myNet.getRow(myNet).set(CyNetwork.NAME,
                              namingUtil.getSuggestedNetworkTitle("My Network"));
        
        // Add two nodes to the network
        CyNode node1 = myNet.addNode();
        CyNode node2 = myNet.addNode();
        
        // set name for new nodes
        myNet.getDefaultNodeTable().getRow(node1.getSUID()).set("name", "Node1");
        myNet.getDefaultNodeTable().getRow(node2.getSUID()).set("name", "Node2");
        
        // Add an edge
        myNet.addEdge(node1, node2, true);
                                
        
        //Creation on the view
        if (myNet == null)
            return;
        this.netMgr.addNetwork(myNet);

        final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(myNet);
        CyNetworkView myView = null;
	    if(views.size() != 0)
	            myView = views.iterator().next();
	    
	    if (myView == null) {
	            // create a new view for my network
	            myView = cnvf.createNetworkView(myNet);
	            networkViewManager.addNetworkView(myView);
	    } else {
	            System.out.println("networkView already existed.");
	    }
        
        // Set the variable destroyNetwork to true, the following code will destroy a network
        boolean destroyNetwork = false;
        if (destroyNetwork){
                // Destroy it
                 netMgr.destroyNetwork(myNet);                        
        }		
	}
	
}

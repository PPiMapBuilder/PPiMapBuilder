package tk.nomis_tech.ppimapbuilder.network;

import java.io.IOException;
import java.io.InputStream;
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
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

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
		String id = "brca2";
		Collection<BinaryInteraction> binaryInteractions = getBinaryInteractionsFromPsicquicQuery(id);
		createNetworkFromBinaryInteractions (binaryInteractions);
	}
	
	public void createNetworkFromBinaryInteractions (Collection<BinaryInteraction> binaryInteractions) {
		// Create an empty network
        CyNetwork myNet = cnf.createNetwork();
        myNet.getRow(myNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("My Network"));
        
        // Add nodes
        CyNode node1, node2 = null;
        for (BinaryInteraction interaction : binaryInteractions) {
        	
        	System.out.println(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier()+
        			"\t"+interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
        	
        	// Add nodes
        	node1 = myNet.addNode();
        	myNet.getDefaultNodeTable().getRow(node1.getSUID()).set("name", interaction.getInteractorA().getIdentifiers().get(0).getIdentifier());
        	node2 = myNet.addNode();
        	myNet.getDefaultNodeTable().getRow(node2.getSUID()).set("name", interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
        	
        	// Add edges
        	myNet.addEdge(node1, node2, true);
        }
        
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

	}
	
	public Collection<BinaryInteraction> getBinaryInteractionsFromPsicquicQuery(String id) {
		Collection<BinaryInteraction> binaryInteractions = null;
		
		try {
			PsicquicSimpleClient client = new PsicquicSimpleClient(
					"http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/");

			PsimiTabReader mitabReader = new PsimiTabReader();

			InputStream result = client.getByQuery(id);

			binaryInteractions = mitabReader.read(result);

			System.out.println("Interactions found: " + binaryInteractions.size());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (PsimiTabException e1) {
			e1.printStackTrace();
		}
		
		return binaryInteractions;
	}
	
}

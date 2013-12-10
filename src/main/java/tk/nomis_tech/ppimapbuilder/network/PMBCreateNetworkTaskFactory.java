package tk.nomis_tech.ppimapbuilder.network;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBCreateNetworkTaskFactory extends AbstractTaskFactory {
	private final CyNetworkManager netMgr;
    private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;
    private final CyNetworkViewFactory cnvf;
    private final CyNetworkViewManager networkViewManager;	
    private final CyLayoutAlgorithmManager layoutMan;
    private final VisualMappingManager vmm;
	
    public PMBCreateNetworkTaskFactory(final CyNetworkNaming cyNetworkNaming, final CyNetworkFactory cnf,final CyNetworkManager networkManager, final CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager, final CyLayoutAlgorithmManager layoutManagerServiceRef, final VisualMappingManager visualMappingManager){
            this.netMgr = networkManager;
            this.namingUtil = cyNetworkNaming;
            this.cnf = cnf;
            this.cnvf = cnvf;
            this.networkViewManager = networkViewManager;
            this.layoutMan = layoutManagerServiceRef;
            this.vmm = visualMappingManager;
    }
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PMBCreateNetworkTask(netMgr, namingUtil, cnf, cnvf, networkViewManager, layoutMan, vmm));
	}

}

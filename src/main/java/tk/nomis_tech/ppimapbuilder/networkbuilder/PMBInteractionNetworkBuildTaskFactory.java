package tk.nomis_tech.ppimapbuilder.networkbuilder;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntryCollection;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import tk.nomis_tech.ppimapbuilder.networkbuilder.query.PMBQueryInteractionTaskMonitor;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;

/**
 * PPiMapBuilder network query and build
 */
public class PMBInteractionNetworkBuildTaskFactory extends AbstractTaskFactory {

	// Cytoscape services
	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyLayoutAlgorithmManager layoutMan;
	private final VisualMappingManager vmm;

	// Data input from the user
	private final QueryWindow queryWindow;

	// Data collection for network generation
	private final UniProtEntryCollection proteinOfInterestPool; // not the same as user input
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntryCollection interactorPool;

	// Error output
	private String error_message;

	public PMBInteractionNetworkBuildTaskFactory(final CyNetworkNaming cyNetworkNaming, final CyNetworkFactory cnf,
			final CyNetworkManager networkManager, final CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager,
			final CyLayoutAlgorithmManager layoutManagerServiceRef, final VisualMappingManager visualMappingManager,
			final QueryWindow queryWindow, final CyTableFactory tableFactory,
			final MapTableToNetworkTablesTaskFactory mapTableToNetworkTablesTaskFactory) {

		this.netMgr = networkManager;
		this.namingUtil = cyNetworkNaming;
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.layoutMan = layoutManagerServiceRef;
		this.vmm = visualMappingManager;
		this.queryWindow = queryWindow;

		this.interactionsByOrg = new HashMap<Organism, Collection<EncoreInteraction>>();
		this.interactorPool = new UniProtEntryCollection();
		this.proteinOfInterestPool = new UniProtEntryCollection();
		
		this.error_message = null;
	}

	@Override
	public TaskIterator createTaskIterator() {
		long startTime = System.currentTimeMillis();
		return new TaskIterator(
			new PMBQueryInteractionTaskMonitor(interactionsByOrg, interactorPool, proteinOfInterestPool, queryWindow),
			new PMBCreateNetworkTask(this, netMgr, namingUtil, cnf, cnvf, networkViewManager, layoutMan, vmm, interactionsByOrg, interactorPool, proteinOfInterestPool, queryWindow, startTime)
		);
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}
	
	public String getError_message() {
		return error_message;
	}
	
}

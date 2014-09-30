package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import ch.picard.ppimapbuilder.networkbuilder.query.PMBQueryInteractionTask;
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
	private final NetworkQueryParameters networkQueryParameters;

	// Data collection for network generation
	private final UniProtEntrySet proteinOfInterestPool; // not the same as user input
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

	// Error output
	private String error_message;

	public PMBInteractionNetworkBuildTaskFactory(final CyNetworkNaming cyNetworkNaming, final CyNetworkFactory cnf,
			final CyNetworkManager networkManager, final CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager,
			final CyLayoutAlgorithmManager layoutManagerServiceRef, final VisualMappingManager visualMappingManager,
			final NetworkQueryParameters networkQueryParameters, final CyTableFactory tableFactory,
			final MapTableToNetworkTablesTaskFactory mapTableToNetworkTablesTaskFactory) {

		this.netMgr = networkManager;
		this.namingUtil = cyNetworkNaming;
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.layoutMan = layoutManagerServiceRef;
		this.vmm = visualMappingManager;
		this.networkQueryParameters = networkQueryParameters;

		this.interactionsByOrg = new HashMap<Organism, Collection<EncoreInteraction>>();
		this.interactorPool = new UniProtEntrySet();
		this.proteinOfInterestPool = new UniProtEntrySet();
		
		this.error_message = null;
	}

	@Override
	public TaskIterator createTaskIterator() {
		long startTime = System.currentTimeMillis();

		interactionsByOrg.clear();
		interactorPool.clear();
		proteinOfInterestPool.clear();

		return new TaskIterator(
			new PMBQueryInteractionTask(interactionsByOrg, interactorPool, proteinOfInterestPool, networkQueryParameters),
			new PMBCreateNetworkTask(this, netMgr, namingUtil, cnf, cnvf, networkViewManager, layoutMan, vmm, interactionsByOrg, interactorPool, proteinOfInterestPool, networkQueryParameters, startTime)
		);
	}

	public void setErrorMessage(String error_message) {
		this.error_message = error_message;
	}
	
	public String getErrorMessage() {
		return error_message;
	}
	
}

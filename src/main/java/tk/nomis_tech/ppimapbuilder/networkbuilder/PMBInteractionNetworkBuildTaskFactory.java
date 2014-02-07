package tk.nomis_tech.ppimapbuilder.networkbuilder;

import java.util.ArrayList;
import java.util.Collection;

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

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import tk.nomis_tech.ppimapbuilder.networkbuilder.query.PMBQueryInteractionTask;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;

/**
 * PPiMapBuilder network query and build
 */
public class PMBInteractionNetworkBuildTaskFactory extends AbstractTaskFactory {

	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyLayoutAlgorithmManager layoutMan;
	private final VisualMappingManager vmm;
	private final Collection<BinaryInteraction> interactionResults;
	private QueryWindow queryWindow;
	private final CyTableFactory tableFactory;
	private final MapTableToNetworkTablesTaskFactory mapTableToNetworkTablesTaskFactory;

	public PMBInteractionNetworkBuildTaskFactory(final CyNetworkNaming cyNetworkNaming,
		final CyNetworkFactory cnf, final CyNetworkManager networkManager,
		final CyNetworkViewFactory cnvf,
		final CyNetworkViewManager networkViewManager,
		final CyLayoutAlgorithmManager layoutManagerServiceRef,
		final VisualMappingManager visualMappingManager,
		final QueryWindow queryWindow,
		final CyTableFactory tableFactory,
		final MapTableToNetworkTablesTaskFactory mapTableToNetworkTablesTaskFactory) {

		this.netMgr = networkManager;
		this.namingUtil = cyNetworkNaming;
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.layoutMan = layoutManagerServiceRef;
		this.vmm = visualMappingManager;
		this.queryWindow = queryWindow;
		this.tableFactory = tableFactory;
		this.mapTableToNetworkTablesTaskFactory = mapTableToNetworkTablesTaskFactory;
		this.interactionResults = new ArrayList<BinaryInteraction>();
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBQueryInteractionTask(interactionResults, queryWindow),
			new PMBCreateNetworkTask(netMgr, namingUtil, cnf, cnvf,
				networkViewManager, layoutMan, vmm, interactionResults)
		);
	}

}

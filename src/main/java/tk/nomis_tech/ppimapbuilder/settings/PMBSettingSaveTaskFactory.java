package tk.nomis_tech.ppimapbuilder.settings;

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
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBSettingSaveTaskFactory extends AbstractTaskFactory {
	
	public PMBSettingSaveTaskFactory() {
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBSettingSaveTask()
		);
	}

}

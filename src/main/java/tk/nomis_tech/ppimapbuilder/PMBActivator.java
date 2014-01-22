package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import tk.nomis_tech.ppimapbuilder.ui.SettingWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * The starting point of the plug-in
 */
public class PMBActivator extends AbstractCyActivator {

	public static BundleContext context;
	public static List<Organism> listOrganism;

	public PMBActivator() {
		super();
		listOrganism = Arrays.asList(new Organism[]{
			new Organism("Homo sapiens", 9606),
			new Organism("Arabidopsis thaliana", 3702),
			new Organism("Caenorhabditis elegans", 6239),
			new Organism("Drosophila Melanogaster", 7227),
			new Organism("Mus musculus", 10090),
			new Organism("Saccharomyces cerevisiae", 4932),
			new Organism("Schizosaccharomyces pombe", 4896)
		});
	}

	/**
	 * This methods register all services of PPiMapBuilder
	 *
	 * @param bc
	 */
	@Override
	public void start(BundleContext bc) {
		context = bc;
//
//		//QueryWindow
		QueryWindow queryWindow = new QueryWindow();
		SettingWindow settingWindow = new SettingWindow();

		PMBInteractionNetworkBuildTaskFactory createNetworkfactory;
		TaskManager networkBuildTaskManager;
		{
			// Network services
			CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);
			CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
			CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

			// View services
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
			CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);

			// Layout services
			CyLayoutAlgorithmManager layoutManagerServiceRef = getService(bc, CyLayoutAlgorithmManager.class);

			// Visual Style services
			VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);

			// Data Table management
			CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
			MapTableToNetworkTablesTaskFactory mapTableToNetworkTablesTaskFactory = getService(bc, MapTableToNetworkTablesTaskFactory.class);

			// Network creation task factory
			createNetworkfactory = new PMBInteractionNetworkBuildTaskFactory(cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, layoutManagerServiceRef, visualMappingManager, queryWindow, tableFactory, mapTableToNetworkTablesTaskFactory);
			queryWindow.setCreateNetworkfactory(createNetworkfactory);
			registerService(bc, createNetworkfactory, TaskFactory.class, new Properties());
			networkBuildTaskManager = getService(bc, TaskManager.class);
			queryWindow.setTaskManager(networkBuildTaskManager);
		}

		PMBQueryMenuTaskFactory queryWindowTaskFactory = new PMBQueryMenuTaskFactory(queryWindow);
		Properties props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Query");
		registerService(bc, queryWindowTaskFactory, TaskFactory.class, props);
		
		PMBSettingMenuTaskFactory settingsWindowTaskFactory = new PMBSettingMenuTaskFactory(settingWindow);
		props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Settings");
		registerService(bc, settingsWindowTaskFactory, TaskFactory.class, props);

	}
}

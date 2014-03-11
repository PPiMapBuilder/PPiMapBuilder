package tk.nomis_tech.ppimapbuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

import tk.nomis_tech.ppimapbuilder.action.ResultPanelAction;
import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.settings.PMBSettingSaveTaskFactory;
import tk.nomis_tech.ppimapbuilder.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.ui.credits.CreditFrame;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.ui.resultpanel.ResultPanel;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.SettingWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;

/**
 * The starting point of the plug-in
 */
public class PMBActivator extends AbstractCyActivator {

	public static BundleContext context;
	public static List<Organism> listOrganism;

	public PMBActivator() {
		super();
		listOrganism = Arrays.asList(new Organism[] { new Organism("Homo sapiens", 9606), new Organism("Arabidopsis thaliana", 3702),
				new Organism("Caenorhabditis elegans", 6239), new Organism("Drosophila Melanogaster", 7227), new Organism("Mus musculus", 10090),
				new Organism("Saccharomyces cerevisiae", 4932), new Organism("Schizosaccharomyces pombe", 4896) });
	}

	/**
	 * This methods register all services of PPiMapBuilder
	 * @param bc
	 */
	@Override
	public void start(BundleContext bc) {
		context = bc;
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);

		// QueryWindow
		QueryWindow queryWindow = new QueryWindow();
		SettingWindow settingWindow = new SettingWindow();
		CreditFrame creditWindow = new CreditFrame(openBrowser);

		// Settings
		PMBSettings.readSettings();

		// Task factory
		PMBInteractionNetworkBuildTaskFactory createNetworkfactory;
		PMBSettingSaveTaskFactory saveSettingFactory;
		PMBCreditMenuTaskFactory creditMenuFactory;
		{
			TaskManager networkBuildTaskManager = getService(bc, TaskManager.class);
			
			// Network services
			CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);
			CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
			CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

			// Result panel (protein & interaction s)
			CySwingApplication cytoscapeDesktopService = getService(bc, CySwingApplication.class);
			cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).setState(CytoPanelState.DOCK);
			
			ResultPanel pmbResultPanel = new ResultPanel(openBrowser);
			registerService(bc, pmbResultPanel, CytoPanelComponent.class, new Properties());

			int index = cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).indexOfComponent(pmbResultPanel);
			if (index > 0)
				cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).setSelectedIndex(index);

			// Result panel action
			ResultPanelAction rpa = new ResultPanelAction(pmbResultPanel);
			registerService(bc, rpa, RowsSetListener.class, new Properties());

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
			createNetworkfactory = new PMBInteractionNetworkBuildTaskFactory(cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
					cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, layoutManagerServiceRef, visualMappingManager, queryWindow, tableFactory,
					mapTableToNetworkTablesTaskFactory);
			queryWindow.setCreateNetworkfactory(createNetworkfactory);
			registerService(bc, createNetworkfactory, TaskFactory.class, new Properties());
			queryWindow.setTaskManager(networkBuildTaskManager);

			// Save settings task factory
			saveSettingFactory = new PMBSettingSaveTaskFactory();
			settingWindow.setSaveSettingFactory(saveSettingFactory);
			registerService(bc, saveSettingFactory, TaskFactory.class, new Properties());
			settingWindow.setTaskManager(networkBuildTaskManager);
			
			// Credit task factory
			creditMenuFactory = new PMBCreditMenuTaskFactory(creditWindow);
			registerService(bc, creditMenuFactory, TaskFactory.class, new Properties());

		}

		// Query window menu
		PMBQueryMenuTaskFactory queryWindowTaskFactory = new PMBQueryMenuTaskFactory(queryWindow);
		Properties props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Query");
		registerService(bc, queryWindowTaskFactory, TaskFactory.class, props);

		// Setting window menu
		PMBSettingMenuTaskFactory settingsWindowTaskFactory = new PMBSettingMenuTaskFactory(settingWindow);
		props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Settings");
		registerService(bc, settingsWindowTaskFactory, TaskFactory.class, props);
		
		// Credits window menu
		PMBCreditMenuTaskFactory creditWindowTaskFactory = new PMBCreditMenuTaskFactory(creditWindow);
		props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Credits");
		registerService(bc, creditWindowTaskFactory, TaskFactory.class, props);

		System.out.println("[PPiMapBuilder] Started.");
	}
}

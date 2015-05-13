/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.data.settings.PMBSettingSaveTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.layout.PMBGOSlimLayoutTaskFactory;
import ch.picard.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import ch.picard.ppimapbuilder.style.PMBVisualStyleTaskFactory;
import ch.picard.ppimapbuilder.ui.credits.CreditFrame;
import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.ui.resultpanel.ResultPanel;
import ch.picard.ppimapbuilder.ui.resultpanel.listener.ResultPanelAction;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The starting point of the plug-in
 */
public class PMBActivator extends AbstractCyActivator {

	public static BundleContext context;

	public static final String version = PMBActivator.class.getPackage().getImplementationVersion();
	public static final boolean isSnapshot = version != null && version.contains("SNAPSHOT");
	public static final String buildTimestamp = getBuildTimestamp();

	public PMBActivator() {
		super();
	}

	private static PMBBackgroundTaskManager pmbBackgroundTaskManager;

	public static PMBBackgroundTaskManager getPMBBackgroundTaskManager() {
		return pmbBackgroundTaskManager;
	}

	/**
	 * This methods register all services of PPiMapBuilder
	 *
	 * @param bc
	 */
	@Override
	public void start(BundleContext bc) {
		context = bc;
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);

		// QueryWindow
		QueryWindow queryWindow = new QueryWindow();
		SettingWindow settingWindow = new SettingWindow(openBrowser);
		CreditFrame creditWindow = new CreditFrame(openBrowser);

		// Will load PMBSettings
		PMBSettings.getInstance();

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
			CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);

			// Result panel (protein & interaction)
			CySwingApplication cytoscapeDesktopService = getService(bc, CySwingApplication.class);
			cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).setState(CytoPanelState.DOCK);

			ResultPanel pmbResultPanel = new ResultPanel(openBrowser);
			registerService(bc, pmbResultPanel, CytoPanelComponent.class, new Properties());

			pmbBackgroundTaskManager = new PMBBackgroundTaskManager(pmbResultPanel);

			int index = cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).indexOfComponent(pmbResultPanel);
			if (index > 0)
				cytoscapeDesktopService.getCytoPanel(CytoPanelName.EAST).setSelectedIndex(index);

			// Result panel action
			ResultPanelAction rpa = new ResultPanelAction(pmbResultPanel, cyApplicationManager);
			registerService(bc, rpa, RowsSetListener.class, new Properties());

			
			// View services
			CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
			CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);


			// Visual Style services
			VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
			VisualMappingFunctionFactory vmfFactoryD = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
			VisualMappingFunctionFactory vmfFactoryP = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
			VisualStyleFactory visualStyleFactoryServiceRef = getService(bc, VisualStyleFactory.class);

			// Visual Style task
			PMBVisualStyleTaskFactory visualStyleFactory = new PMBVisualStyleTaskFactory(visualMappingManager, vmfFactoryD, vmfFactoryP, visualStyleFactoryServiceRef);
			networkBuildTaskManager.execute(visualStyleFactory.createTaskIterator());
			

			// Layout services
			CyLayoutAlgorithmManager layoutManagerServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
			{
				PMBGOSlimLayoutTaskFactory applyLayoutTaskFactory = new PMBGOSlimLayoutTaskFactory(layoutManagerServiceRef);
				Properties applyCustomLayoutProperties = new Properties();
				applyCustomLayoutProperties.setProperty("preferredMenu", "Layout");
				applyCustomLayoutProperties.setProperty("title", "PMB Layout");
				registerService(bc, applyLayoutTaskFactory, NetworkViewTaskFactory.class, applyCustomLayoutProperties);
			}

			// Network creation task factory
			createNetworkfactory = new PMBInteractionNetworkBuildTaskFactory(
					cyNetworkManagerServiceRef, cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef,
					cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, layoutManagerServiceRef,
					visualMappingManager, queryWindow
			);
			queryWindow.setCreateNetworkFactory(createNetworkfactory);
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

	private static String getBuildTimestamp() {
		BufferedReader br = new BufferedReader(new InputStreamReader(PMBActivator.class.getResourceAsStream("/META-INF/MANIFEST.MF")));

		Pattern pattern = Pattern.compile("Build-Time:\\s+(.*)");
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if(matcher.matches())
					return matcher.group(1);
			}
		} catch (IOException ignored) {}

		return "";
	}
}

package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import java.util.Properties;

/**
 * The starting point of the plug-in
 */
public class CyActivator extends AbstractCyActivator {
	
	public static BundleContext context;
	
	public CyActivator() {
		super();
	}

	/**
	 * This methods register all services of PPiMapBuilder
	 */
	public void start(BundleContext bc) {
		context = bc;
		PMBMenuFactory factory = new PMBMenuFactory();
		
		Properties props = new Properties();
		props.setProperty("preferredMenu", "Apps.PPiMapBuilder");
		props.setProperty("title", "Query");
		registerService(bc, factory, TaskFactory.class, props);
	}
}


package tk.nomis_tech.ppimapbuilder;

import java.util.Properties;

import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {
		/*
		 * CyApplicationManager cyApplicationManager = getService(bc,
		 * CyApplicationManager.class);
		 * 
		 * MenuAction action = new MenuAction(cyApplicationManager,
		 * "Hello World App TT");
		 * 
		 * Properties properties = new Properties();
		 * 
		 * registerAllServices(bc, action, properties);
		 */

		WebServiceHelper webServiceHelper = new WebServiceHelper();
		UseWebServiceTaskFactory useWebServiceTaskFactory = new UseWebServiceTaskFactory(
				webServiceHelper);

		Properties useWebServiceTaskFactoryProps = new Properties();
		useWebServiceTaskFactoryProps.setProperty("preferredMenu",
				"Apps.Samples");
		useWebServiceTaskFactoryProps.setProperty("title", "Use Web Service");
		registerService(bc, useWebServiceTaskFactory, TaskFactory.class,
				useWebServiceTaskFactoryProps);

		registerServiceListener(bc, webServiceHelper, "addWebServiceClient",
				"removeWebServiceClient", WebServiceClient.class);

	}

}

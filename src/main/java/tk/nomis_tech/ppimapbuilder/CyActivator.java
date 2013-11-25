package tk.nomis_tech.ppimapbuilder;

import java.util.Properties;

import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {

		WebServiceHelper webServiceHelper = new WebServiceHelper();
		TaskFactory useWebServiceTaskFactory = new TaskFactory(
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

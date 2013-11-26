package tk.nomis_tech.ppimapbuilder;

import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import java.util.Properties;
import org.cytoscape.io.webservice.WebServiceClient;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		WebServiceHelper webServiceHelper = new WebServiceHelper();		
		MenuTaskFactory useWebServiceTaskFactory= new MenuTaskFactory(webServiceHelper);
		
		Properties useWebServiceTaskFactoryProps = new Properties();
		useWebServiceTaskFactoryProps.setProperty("preferredMenu","Apps.PPiMaPBuilder");
		useWebServiceTaskFactoryProps.setProperty("title","Test");
		registerService(bc,useWebServiceTaskFactory,TaskFactory.class, useWebServiceTaskFactoryProps);

		registerServiceListener(bc,webServiceHelper,"addWebServiceClient","removeWebServiceClient",WebServiceClient.class);
	}
}


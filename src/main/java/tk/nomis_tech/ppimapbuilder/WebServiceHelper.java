package tk.nomis_tech.ppimapbuilder;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.webservice.WebServiceClient;

/**
 * Class keeping track of cytoscape web services
 */
public class WebServiceHelper {

	private final Map<WebServiceClient, Map> webserviceMap;

	public WebServiceHelper() {
		webserviceMap = new HashMap<WebServiceClient, Map>();
	}

	public void addWebServiceClient(final WebServiceClient newFactory,
			final Map properties) {
		webserviceMap.put(newFactory, properties);
	}

	public void removeWebServiceClient(final WebServiceClient factory,
			final Map properties) {
		webserviceMap.remove(factory);
	}

	public Map<WebServiceClient, Map> getWebserviceMap() {
		return webserviceMap;
	}
}
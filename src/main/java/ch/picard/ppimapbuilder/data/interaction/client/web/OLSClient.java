package ch.picard.ppimapbuilder.data.interaction.client.web;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.util.*;

import ch.picard.ppimapbuilder.data.settings.PMBSettings;

public class OLSClient {
	
	private static OLSClient _instance;
	private LinkedHashMap<String, String> psiMiCache;

	public static OLSClient getInstance() {
		if (_instance == null)
			_instance = new OLSClient();
		return _instance;
	}
	
	private OLSClient() {
		this.psiMiCache = new LinkedHashMap<String, String>();
	}
	
	public List<String> convert(Collection<String> list) {
		List<String> retList = new ArrayList<String>();

		if (!list.isEmpty()) {
						    
			for (String elt : list){
				if (psiMiCache.containsKey(elt)) {
					retList.add(psiMiCache.get(elt));
				}
				else {
					try {
						QueryService locator = new QueryServiceLocator();
						Query qs = locator.getOntologyQuery();
						retList.add(qs.getTermById(elt, "MI"));
					    
					} catch (Exception e) {
					    System.out.println("Can't connect to Ontology lookup service");
					}
				}	
			}
		
		}
		
		return retList;
		
	}
	
	

}

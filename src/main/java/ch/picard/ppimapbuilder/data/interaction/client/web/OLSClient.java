package ch.picard.ppimapbuilder.data.interaction.client.web;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;


public class OLSClient {
	
	private static OLSClient _instance;
	private LinkedHashMap<String, String> psiMiCache;
	private final File olsCache;

	public static OLSClient getInstance() {
		if (_instance == null)
			_instance = new OLSClient();
		return _instance;
	}
	
	private OLSClient() {
		this.psiMiCache = new LinkedHashMap<String, String>();
		this.olsCache = new File(new File(new File(System.getProperty("user.home"), "CytoscapeConfiguration"), "PPiMapBuilder"), "ols-cache.dat");
		load();
	}
	
	private void load() {
		if (this.olsCache.exists()) {
			ObjectInputStream fileIn = null;
			try {
				fileIn = new ObjectInputStream(new FileInputStream(this.olsCache));
				try {
					this.psiMiCache = (LinkedHashMap<String, String>) fileIn.readObject();
				} catch (Exception ignored) {}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fileIn != null)
					try {
						fileIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	private void save() {
		ObjectOutputStream fileOut = null;
		try {
            if(!this.olsCache.exists())
            	this.olsCache.createNewFile();
			fileOut = new ObjectOutputStream(new FileOutputStream(this.olsCache));
			fileOut.writeObject(this.psiMiCache);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
						String res = qs.getTermById(elt, "MI");
						retList.add(res);
						psiMiCache.put(elt, res);
					    
					} catch (Exception e) {
					    System.out.println("Can't connect to Ontology lookup service");
					}
				}	
			}
		}
		save();
		return retList;
	}
	
	

}

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
    
package ch.picard.ppimapbuilder.data.interaction.client.web;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


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

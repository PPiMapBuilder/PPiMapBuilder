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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Retrieve the PSICQUIC registry in a very simple way. TODO: Use Registry and XML file
 */
public class PsicquicRegistry {

	/**  Singleton instance*/
	private static PsicquicRegistry _instance;
	
	/** The base URL for PSICQUIC registry. */
	private static final String registryXmlUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=xml";
	
	/** Structure to organism service names and services URLs. */
	private final ArrayList<PsicquicService> services = new ArrayList<PsicquicService>();

	/** Private constructor */
	private PsicquicRegistry() {}

	public static PsicquicRegistry getInstance() {
		if(_instance == null)
			_instance = new PsicquicRegistry();
		return _instance;
	}

	public int size() {
		return services.size();
	}

	public boolean isEmpty() {
		return services.isEmpty();
	}

	public boolean contains(Object o) {
		return services.contains(o);
	}

	/**
	 * Gets a PSICQUIC service by its name
	 */
	public PsicquicService getService(String name, boolean caseSensitive) {
		if(services.isEmpty()) {
			try {
				retrieveServices();
			} catch (IOException e) {}
		}
		
		for (PsicquicService service : services) {
			if (caseSensitive && name.equals(service.getName()))
				return service;
			else if(!caseSensitive && name.equalsIgnoreCase(service.getName()))
				return service;
		}
		return null;
	}

	public boolean add(PsicquicService e) {
		return services.add(e);
	}

	public PsicquicService remove(int index) {
		return services.remove(index);
	}

	public boolean remove(Object o) {
		return services.remove(o);
	}

	public boolean remove(String name) {
		for (PsicquicService psicquicService : services) {
			if (psicquicService.getName().equalsIgnoreCase(name)) {
				return services.remove(psicquicService);
			}
		}
		return false;
	}

	public void clear() {
		services.clear();
	}

	public ListIterator<PsicquicService> listIterator(int index) {
		return services.listIterator(index);
	}

	public ListIterator<PsicquicService> listIterator() {
		return services.listIterator();
	}

	public Iterator<PsicquicService> iterator() {
		return services.iterator();
	}

	/**
	 * Get the services list.
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public synchronized List<PsicquicService> getServices() throws IOException {
		if (services.isEmpty()) {
			this.retrieveServices();
		}
		return this.services;
	}

	/**
	 * Retrieve actives services according to the Format.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public synchronized void retrieveServices() throws IOException {
		this.services.clear();

		Document doc = Jsoup.connect(registryXmlUrl).get();
		for (Element e : doc.select("service")) {

			List<String> tags = new ArrayList<String>();
			for (Element elt : e.select("tag")) {
				tags.add(elt.text());
			}

			services.add(new PsicquicService(e.select("name").first().text(), e.select("soapUrl").first().text(), e.select("restUrl")
					.first().text(), e.select("active").first().text(), e.select("count").first().text(), e.select("version").first()
					.text(), e.select("organizationUrl").first().text(), e.select("restricted").first().text(), tags));
		}
	}
}

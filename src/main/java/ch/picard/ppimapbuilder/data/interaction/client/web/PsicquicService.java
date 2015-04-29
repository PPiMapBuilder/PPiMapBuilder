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

import java.util.Iterator;
import java.util.List;

public class PsicquicService {

	final private String name;
	final private String soapUrl;
	final private String restUrl;
	final private boolean active;
	final private long count;
	final private String version;
	final private String organizationUrl;
	final private boolean restricted;
	final private List<String> tags;

	public PsicquicService(String name, String soapUrl, String restUrl, String active, String count, String version, String organizationUrl, String restricted, List<String> tags) {
		this.name = name;
		this.soapUrl = soapUrl;
		this.restUrl = restUrl;
		this.active = active.equals("true");
		this.count = Integer.parseInt(count);
		this.version = version;
		this.organizationUrl = organizationUrl;
		this.restricted = restricted.equals("true");
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public String getSoapUrl() {
		return soapUrl;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public boolean isActive() {
		return active;
	}

	public long getCount() {
		return count;
	}

	public String getVersion() {
		return version;
	}

	public String getOrganizationUrl() {
		return organizationUrl;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public List<String> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PsicquicService{" + "name=" + name + ", soapUrl=" + soapUrl + ", restUrl=" + restUrl + ", active=" + active + ", count=" + count + ", version=" + version + ", organizationUrl=" + organizationUrl + ", restricted=" + restricted + ", tags=");

		sb.append('[');
		for (Iterator<String> it = tags.iterator(); it.hasNext(); ) {
			String t = it.next();
			sb.append(t);
			if (it.hasNext()) {
				sb.append(',');
			}
		}
		sb.append("]}");
		return sb.toString();
	}

}


/*
 <service>
 <name>APID</name>
 <soapUrl>
 http://cicblade.dep.usal.es/interaction-ws/webservices/interaction
 </soapUrl>
 <restUrl>
 http://cicblade.dep.usal.es/interaction-ws/webservices/current/search/
 </restUrl>
 <restExample>
 http://cicblade.dep.usal.es/interaction-ws/webservices/current/search/interactor/P00533
 </restExample>
 <active>true</active>
 <count>416124</count>
 <version>1.1.5</version>
 <organizationUrl>http://bioinfow.dep.usal.es/apid/index.htm</organizationUrl>
 <restricted>false</restricted>
 <tag>MI:1047</tag>
 <tag>MI:1058</tag>
 <tag>MI:1060</tag>
 <tag>MI:1052</tag>
 </service>
 */

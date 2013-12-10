package tk.nomis_tech.ppimapbuilder.util;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Kévin Gravouil
 */
public class PsicquicService {

    protected String name;
    protected String soapUrl;
    protected String restUrl;
    protected boolean active;
    protected long count;
    protected String version;
    protected String organizationUrl;
    protected boolean restricted;
    protected List<String> tags;

    public PsicquicService(String name, String soapUrl, String restUrl, String active, String count, String version, String organizationUrl, String restricted, List<String> tags) {
        this.name = name;
        this.soapUrl = soapUrl;
        this.restUrl = restUrl;
        this.active = active.equals("y");
        this.count = Integer.parseInt(count);
        this.version = version;
        this.organizationUrl = organizationUrl;
        this.restricted = restricted.equals("y");
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
        for (Iterator<String> it = tags.iterator(); it.hasNext();) {
            String t = it.next();
            sb.append(t);
            if(it.hasNext()) {
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
 http://cicblade.dep.usal.es/psicquic-ws/webservices/psicquic
 </soapUrl>
 <restUrl>
 http://cicblade.dep.usal.es/psicquic-ws/webservices/current/search/
 </restUrl>
 <restExample>
 http://cicblade.dep.usal.es/psicquic-ws/webservices/current/search/interactor/P00533
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

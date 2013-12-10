package tk.nomis_tech.ppimapbuilder.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Retrieve the PSICQUIC registry in a very simple way. TODO: Use Registry and
 * XML file
 *
 * @author Kevin Gravouil, Guillaume Cornut
 */
public class PsicquicRegistry {

    /**
     * The base URL for PSICQUIC registry.
     */
    private static final String registryXmlUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=xml";
    /**
     * Structure to store service names and services URLs.
     */
    private final ArrayList<PsicquicService> services = new ArrayList<PsicquicService>();

    ;

    /**
     * Create the PsicquicRegistry object.
     *
     */
    public PsicquicRegistry() {
    }

    /**
     * Get the services list.
     *
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public List<PsicquicService> getServices() throws IOException {
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
    private void retrieveServices() throws IOException {
        this.services.clear();

        Document doc = Jsoup.connect(registryXmlUrl).get();
        for (Element e : doc.select("service")) {
            services.add(new PsicquicService(e.select("name").first().text(), e.select("restUrl").first().text()));
        }

    }

    public int size() {
        return services.size();
    }

    public boolean isEmpty() {
        return services.isEmpty();
    }

    public boolean add(PsicquicService e) {
        return services.add(e);
    }
}

package tk.nomis_tech.ppimapbuilder.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Retrieve the PSICQUIC registry in a very simple way. TODO: Use Registry and
 * XML file
 *
 * @author Kevin Gravouil
 */
public class PsicquicRegistry {

    /**
     * Enum format, TXT or XML.
     */
    public enum Format {

        TXT, XML;
    }

    /**
     * Custom exceptions if Format isnt good.
     */
    public class BadFormatException extends Exception {

        public BadFormatException() {
            this("Bad format exception (txt or xml)");
        }

        public BadFormatException(String message) {
            super(message);
        }

    }
    /**
     * The base URL for PSICQUIC registry.
     */
    private static final String registryTxtUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=txt";
    /**
     * The base URL for PSICQUIC registry.
     */
    private static final String registryXmlUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=xml";
    /**
     * Structure to store service names and services URLs.
     */
    private final HashMap<String, String> services;

    /**
     * Create the PsicquicRegistry object.
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    public PsicquicRegistry() throws MalformedURLException, IOException {
        services = new HashMap<String, String>();
    }

    /**
     * Get the PSICQUIC registry URL according to format (TXT or XML).
     *
     * @param f
     * @return
     * @throws
     * tk.nomis_tech.ppimapbuilder.util.PsicquicRegistry.BadFormatException
     */
    public String getRegistryUrl(Format f) throws BadFormatException {
        switch (f) {
            case TXT:
                return registryTxtUrl;
            case XML:
                return registryXmlUrl;
        }
        throw new BadFormatException();
    }

    /**
     * Get the default PSICQUIC registry URL (txt foramt)
     *
     * @return
     * @throws
     * tk.nomis_tech.ppimapbuilder.util.PsicquicRegistry.BadFormatException
     */
    public String getRegistryUrl() throws BadFormatException {
        return getRegistryUrl(Format.TXT);
    }

    /**
     * Get the services list.
     *
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public HashMap<String, String> getServices() throws IOException, MalformedURLException, BadFormatException {
        if (services.isEmpty()) {
            this.retrieveServices();
        }
        return services;
    }

    /**
     * Retrieve actives services as text.
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    private void retrieveServices() throws IOException, MalformedURLException, BadFormatException {
        this.retrieveServices(Format.TXT);
    }

    /**
     * Retrieve actives services according to the Format.
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    private void retrieveServices(Format f) throws MalformedURLException, BadFormatException, IOException {
        this.services.clear();
        URLConnection yc;
        BufferedReader in;
        URL reg;

        switch (f) {
            case TXT:
                String[] inputLine;
                reg = new URL(registryTxtUrl);
                yc = reg.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine().split("=")) != null) {
                    services.put(inputLine[0], inputLine[1]);
                }
                in.close();
                break;

            case XML: // COMPLETLY USELESS for the moment
                StringBuilder sb = new StringBuilder();
                reg = new URL(registryXmlUrl);
                yc = reg.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                break;
            default:
                throw new BadFormatException();
        }
    }

    public static void main(String[] args) {
    }
}

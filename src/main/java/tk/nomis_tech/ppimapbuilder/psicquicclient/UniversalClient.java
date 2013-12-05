package tk.nomis_tech.ppimapbuilder.psicquicclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.XmlPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.result.MitabSearchResult;

/**
 * Query each PSICQUIC service.
 *
 * @author Kevin Gravouil
 */
public class UniversalClient {

    public HashMap<String, String> services;
    private static UniversalPsicquicClient client = null;
    private static final String url = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=txt";
    private static final String wsIntact = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/psicquic";
    private static final String proxyHost = "http://sox.univ-poitiers.fr";
    private static final Integer proxyPort = 1080;

    public UniversalClient() throws IOException, MalformedURLException, ParseException {

        System.out.println("coucou11");
        System.setProperty("proxySet", "true");
        System.setProperty("http.proxyHost", "cache.univ-poitiers.fr");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("https.proxyHost", "cache.univ-poitiers.fr");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("socksProxyHost", "sox.univ-poitiers.fr");
        System.setProperty("socksProxyPort ", "1080");

//        retrievePsicquicServices();
        System.out.println("coucou12");

        UniversalClient.client = new UniversalPsicquicClient(wsIntact);
        System.out.println("coucou13");

    }

    /**
     * Get a REST URL from the registry
     * http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry?action=ACTIVE&format=txt.
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws Exception
     */
    private void retrievePsicquicServices() throws MalformedURLException, IOException, ParseException {
        this.services = new HashMap<String, String>();
        URLConnection yc = new URL(url).openConnection();
        yc.setConnectTimeout(2000);
        yc.setReadTimeout(2000);
        yc.setAllowUserInteraction(false);
        yc.setDoOutput(true);

        System.out.println("* reading...");
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

        String tmp2;
        while ((tmp2 = in.readLine()) != null) {
            String[] tmp = tmp2.split("=");
            if (tmp == null) {
                throw new ParseException("Error parsing PSICQUIC Registry", in.read());
            }
            this.services.put(tmp[0], tmp[1]);
        }
    }

    public String getInteractionFor(String uniprotId) throws PsicquicClientException {
        return UniversalClient.client.getByInteractor(uniprotId, 1, 200).getRawResults();
    }

    public static void main(String[] args) throws IOException, MalformedURLException, ParseException {
        System.out.println("coucou1");

        UniversalPsicquicClient client = new UniversalPsicquicClient("http://www.ebi.ac.uk/intact/psicquic/webservices/psicquic");
        System.out.println("CLIENT INSTANCE: " + client);
    }

}

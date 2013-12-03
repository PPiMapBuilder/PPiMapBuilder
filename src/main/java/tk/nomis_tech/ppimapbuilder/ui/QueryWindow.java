package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.cytoscape.property.CyProperty;
import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.NotSupportedMethodException;
import org.hupo.psi.mi.psicquic.NotSupportedTypeException;
import org.hupo.psi.mi.psicquic.PsicquicService;
import org.hupo.psi.mi.psicquic.PsicquicServiceException;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.hupo.psi.mi.psicquic.wsclient.AbstractPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import tk.nomis_tech.ppimapbuilder.psicquicclient.UniversalClient;

public class QueryWindow extends JFrame {

    public QueryWindow() {

        setTitle("Interaction Query");

        this.setPreferredSize(new Dimension(50, 50));
        JButton startQuery = new JButton("Start");
        startQuery.setPreferredSize(new Dimension(50, 50));
        startQuery.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // print out proxy settings for debugging purposes
//                System.setProperty("proxySet", "true");
//                System.setProperty("http.proxyHost", "cache.univ-poitiers.fr");
//                System.setProperty("http.proxyPort", "3128");
//                System.setProperty("https.proxyHost", "cache.univ-poitiers.fr");
//                System.setProperty("https.proxyPort", "3128");
//                System.setProperty("socksProxyHost", "sox.univ-poitiers.fr");
//                System.setProperty("socksProxyPort ", "1080");
                UniversalClient uc;
                try {

                    uc = new UniversalClient();
                    System.out.println(uc.getInteractionFor("P07900"));
                    
                } catch (IOException ex) {
                    Logger.getLogger(QueryWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(QueryWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (PsicquicClientException ex) {
                    Logger.getLogger(QueryWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        });
        getContentPane().add(startQuery);

        setLocationRelativeTo(null);
    }
}

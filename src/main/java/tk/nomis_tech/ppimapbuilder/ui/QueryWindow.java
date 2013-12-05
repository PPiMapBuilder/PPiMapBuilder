package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;

import tk.nomis_tech.ppimapbuilder.psicquicclient.UniversalClient;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public QueryWindow() {
		setTitle("Interaction Query");

		JButton startQuery = new JButton("Start");
		startQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// print out proxy settings for debugging purposes
				// System.setProperty("proxySet", "true");
				// System.setProperty("http.proxyHost",
				// "cache.univ-poitiers.fr");
				// System.setProperty("http.proxyPort", "3128");
				// System.setProperty("https.proxyHost",
				// "cache.univ-poitiers.fr");
				// System.setProperty("https.proxyPort", "3128");
				// System.setProperty("socksProxyHost", "sox.univ-poitiers.fr");
				// System.setProperty("socksProxyPort ", "1080");
				UniversalClient uc;
				try {

					uc = new UniversalClient();
					System.out.println(uc.getInteractionFor("P07900"));

				} catch (IOException ex) {
					Logger.getLogger(QueryWindow.class.getName()).log(
							Level.SEVERE, null, ex);
				} catch (ParseException ex) {
					Logger.getLogger(QueryWindow.class.getName()).log(
							Level.SEVERE, null, ex);
				} catch (PsicquicClientException ex) {
					Logger.getLogger(QueryWindow.class.getName()).log(
							Level.SEVERE, null, ex);
				}

			}

		});

		getContentPane().add(startQuery);
		getRootPane().setDefaultButton(startQuery);

		setBounds(0, 0, 200, 100);
		setLocationRelativeTo(null);
	}
}

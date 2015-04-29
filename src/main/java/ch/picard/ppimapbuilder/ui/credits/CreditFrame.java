package ch.picard.ppimapbuilder.ui.credits;

import ch.picard.ppimapbuilder.PMBActivator;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.util.swing.OpenBrowser;
import ch.picard.ppimapbuilder.ui.util.EscapeCloseListener;
import ch.picard.ppimapbuilder.ui.util.JHyperlinkLabel;
import ch.picard.ppimapbuilder.ui.util.LogoIcon;
import ch.picard.ppimapbuilder.ui.util.PicardLabIcon;

import javax.swing.*;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class CreditFrame extends JFrame {

	private static final long serialVersionUID = 1L; // Instance of the ppimapbuilder menu to prevent several instances 

	private EscapeCloseListener escapeListener;

	/**
	 * Default constructor which is private to prevent several instances
	 * Create the entire credits frame
	 * @param openBrowser 
	 */
	public CreditFrame(OpenBrowser openBrowser) {
		super("About PPiMapBuilder");

		Dimension size = new Dimension(400, 400);
		setMaximumSize(size);
		setMinimumSize(size);
		setPreferredSize(size);

		setResizable(false);
		
		JPanel container = new JPanel();

		container.setLayout(new MigLayout("inset 5", "[grow][22px][grow]", "[][][][][][][][][][][][]"));

		// PPiMapBuilder Logo
		JLabel lblLogo = new LogoIcon(openBrowser);
		container.add(lblLogo, "cell 0 1 3,alignx center");

		// PPiMapBuilder Name
		JLabel lblPpimapbuilder = new JLabel("PPiMapBuilder");
		lblPpimapbuilder.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		container.add(lblPpimapbuilder, "cell 0 2 3,alignx center");

		// PPiMapBuilder Version
		String version = "Version " + PMBActivator.version;
		JLabel lblVersion = new JLabel(version);
		lblVersion.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		container.add(lblVersion, "cell 0 3 3,alignx center");


		// PPiMapBuilder email
		JHyperlinkLabel lblEmail = new JHyperlinkLabel(openBrowser);
		lblEmail.setText("<ppimapbuilder@gmail.com>");
		lblEmail.setToolTipText("Send an email");
		lblEmail.makeClickable();
		try {
			lblEmail.setUri(new URI("mailto:ppimapbuilder@gmail.com"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		lblEmail.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblEmail.setForeground(Color.GRAY);
		lblEmail.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		container.add(lblEmail, "cell 0 4 3 1,alignx center");

		// Initiated by
		JLabel lblInitiatedBy = new JLabel("Initiated by:");
		lblInitiatedBy.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		container.add(lblInitiatedBy, "cell 0 6 3 1,alignx center");

		// Pablo Echeverría
		JLabel lblPabloEcheverria = new JLabel("Pablo Echeverría");
		container.add(lblPabloEcheverria, "cell 0 7,alignx center");

		// PicardLab logo
		JLabel lblPicardLabLogo = new PicardLabIcon(openBrowser);
		lblPicardLabLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		container.add(lblPicardLabLogo, "cell 2 7,alignx center");

		// Developpers
		JLabel lblAuthors = new JLabel("Developpers:");
		lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		container.add(lblAuthors, "cell 0 9 3 1,alignx center");
		
		// Guillaume Cornut
		JLabel lblGuillaumeCornut = new JLabel("Guillaume Cornut");
		container.add(lblGuillaumeCornut, "cell 0 10,alignx center");

		// Pierre Dupuis
		JLabel lblPierreDupuis = new JLabel("Pierre Dupuis");
		container.add(lblPierreDupuis, "cell 2 10,alignx center");

		// Kévin Gravouil
		JLabel lblKvinGravouil = new JLabel("Kévin Gravouil");
		container.add(lblKvinGravouil, "cell 0 11,alignx center");
		
		// Armelle Kieffer
		JLabel lblArmelleKieffer = new JLabel("Armelle Kieffer");
		container.add(lblArmelleKieffer, "cell 2 11,alignx center");
		
		// UnivPoitiers logo
		JHyperlinkLabel lblUnivPoitiersLogo = new JHyperlinkLabel(openBrowser);
		try {
			lblUnivPoitiersLogo.setIcon(new ImageIcon(CreditFrame.class.getResource("univ-poitiers.png")));
		} catch (Exception e) {
			lblUnivPoitiersLogo.setText("University of Poitiers");
		}
		lblUnivPoitiersLogo.setToolTipText("Access to master's degree details [fr]");
		lblUnivPoitiersLogo.makeClickable();
		try {
			lblUnivPoitiersLogo.setUri(new URI("http://sfa.univ-poitiers.fr/biosante/spip.php?rubrique86"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		lblUnivPoitiersLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		container.add(lblUnivPoitiersLogo, "cell 0 12 3 1,alignx center");
		
		// Paper
		JLabel lblPaperTitle = new JLabel("Original workflow:");
		lblPaperTitle.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		container.add(lblPaperTitle, "cell 0 13 3 1,alignx center");

		JLabel lblPaper = new JLabel("Echeverría, P. C. et al. (2011), PLoS ONE 6, e26044");
		container.add(lblPaper, "cell 0 14 3 1,alignx center");
		JHyperlinkLabel lblOpenAccess = new JHyperlinkLabel(openBrowser);
		lblOpenAccess.setText("[Open Access]");
		lblOpenAccess.setToolTipText("http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0026044");
		lblOpenAccess.makeClickable();
		try {
			lblOpenAccess.setUri(new URI("http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0026044"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		lblOpenAccess.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lblOpenAccess.setForeground(Color.GRAY);
		lblOpenAccess.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		container.add(lblOpenAccess, "cell 0 15 3 1,alignx center");
		
		JScrollPane jsp = new JScrollPane(container);
		getContentPane().add(jsp);

		// The frame is now centered relatively to Cytoscape
		this.setLocationRelativeTo(null); 

		// Close window on escape key
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		escapeListener = new EscapeCloseListener(this);
		manager.addKeyEventDispatcher(escapeListener);
	}
}

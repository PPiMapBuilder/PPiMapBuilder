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

		setLayout(new MigLayout("inset 5", "[grow][22px][grow]", "[][][][][][][][][][][][]"));

		// PPiMapBuilder Logo
		JLabel lblLogo = new LogoIcon(openBrowser);
		getContentPane().add(lblLogo, "cell 0 1 3,alignx center");

		// PPiMapBuilder Name
		JLabel lblPpimapbuilder = new JLabel("PPiMapBuilder");
		lblPpimapbuilder.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		getContentPane().add(lblPpimapbuilder, "cell 0 2 3,alignx center");

		// PPiMapBuilder Version
		String version = "Version " + PMBActivator.version;
		JLabel lblVersion = new JLabel(version);
		lblVersion.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		getContentPane().add(lblVersion, "cell 0 3 3,alignx center");


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
		getContentPane().add(lblEmail, "cell 0 4 3 1,alignx center");

		// Initiated by
		JLabel lblInitiatedBy = new JLabel("Initiated by:");
		lblInitiatedBy.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		getContentPane().add(lblInitiatedBy, "cell 0 6 3 1,alignx center");

		// Pablo Echeverría
		JLabel lblPabloEcheverria = new JLabel("Pablo Echeverría");
		getContentPane().add(lblPabloEcheverria, "cell 0 7,alignx center");

		// PicardLab logo
		JLabel lblPicardLabLogo = new PicardLabIcon(openBrowser);
		lblPicardLabLogo.setCursor(new Cursor(Cursor.HAND_CURSOR));
		getContentPane().add(lblPicardLabLogo, "cell 2 7,alignx center");

		// Developpers
		JLabel lblAuthors = new JLabel("Developpers:");
		lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		getContentPane().add(lblAuthors, "cell 0 9 3 1,alignx center");

		/*// Guillaume Cornut
		JLabel lblGuillaumeCornutPierre = new JLabel("Guillaume Cornut");
		lblGuillaumeCornutPierre.setToolTipText("Wonderful Gui");
		getContentPane().add(lblGuillaumeCornutPierre, "cell 0 9,alignx center");

		// Pierre Cressant
		JLabel lblPierreCressant = new JLabel("Pierre Cressant");
		lblPierreCressant.setToolTipText("Amazing Piotr");
		getContentPane().add(lblPierreCressant, "cell 2 9,alignx center");

		// Pierre Dupuis
		JLabel lblPierreDupuis = new JLabel("Pierre Dupuis");
		lblPierreDupuis.setToolTipText("Tremendous Boss");
		getContentPane().add(lblPierreDupuis, "cell 0 10,alignx center");

		// Kévin Gravouil
		JLabel lblKvinGravouil = new JLabel("Kévin Gravouil");
		lblKvinGravouil.setToolTipText("Marvelous Keuv");
		getContentPane().add(lblKvinGravouil, "cell 2 10,alignx center");*/
		
		// Guillaume Cornut
		JLabel lblGuillaumeCornut = new JLabel("Guillaume Cornut");
		getContentPane().add(lblGuillaumeCornut, "cell 0 10,alignx center");

		// Pierre Dupuis
		JLabel lblPierreDupuis = new JLabel("Pierre Dupuis");
		getContentPane().add(lblPierreDupuis, "cell 2 10,alignx center");

		// Kévin Gravouil
		JLabel lblKvinGravouil = new JLabel("Kévin Gravouil");
		getContentPane().add(lblKvinGravouil, "cell 0 11,alignx center");
		
		// Armelle Kieffer
		JLabel lblArmelleKieffer = new JLabel("Armelle Kieffer");
		getContentPane().add(lblArmelleKieffer, "cell 2 11,alignx center");

		// The frame is now centered relatively to Cytoscape
		this.setLocationRelativeTo(null); 

		// Close window on escape key
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		escapeListener = new EscapeCloseListener(this);
		manager.addKeyEventDispatcher(escapeListener);
	}
}

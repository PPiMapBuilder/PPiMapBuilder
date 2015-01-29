package ch.picard.ppimapbuilder.ui.util.label;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;

import org.cytoscape.util.swing.OpenBrowser;


/**
 * PicarLab logo icon label
 */
public class PicardLabIcon extends JHyperlinkLabel {
	private static final long serialVersionUID = 1L;

	public PicardLabIcon(OpenBrowser openBrowser) {
		super(openBrowser);

		try {
			setIcon(new ImageIcon(PicardLabIcon.class.getResource("picard_lab.png")));
		} catch (Exception e) {
			setText("PicardLab");
		}

		this.setToolTipText("Access to PicardLab website");
		this.makeClickable();


		try {
			this.setUri(new URI("http://www.picard.ch/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
package tk.nomis_tech.ppimapbuilder.ui.settingwindow;

import org.cytoscape.util.swing.OpenBrowser;
import tk.nomis_tech.ppimapbuilder.ui.util.JHyperlinkLabel;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;

public class InParanoidLogo extends JHyperlinkLabel {

	public InParanoidLogo(OpenBrowser openBrowser) {
		super(openBrowser);

		try {
			setIcon(new ImageIcon(InParanoidLogo.class.getResource("InParanoid.png")));
		} catch (Exception e) {
			setText("InParanoid");
		}

		this.setToolTipText("Access to InParanoid website");
		this.makeClickable();


		try {
			this.setUri(new URI("http://inparanoid.sbc.su.se/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}

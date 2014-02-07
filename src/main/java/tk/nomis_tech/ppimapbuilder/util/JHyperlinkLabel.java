package tk.nomis_tech.ppimapbuilder.util;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.cytoscape.util.swing.OpenBrowser;

@SuppressWarnings("serial")
public class JHyperlinkLabel extends JLabel {

	private URI uri;
	private OpenBrowser openBrowser;

	public JHyperlinkLabel(OpenBrowser openBrowser) {
		super();
//		makeClickable();
	}

	// public JHyperlinkLabel(Icon icn, URI uri) {
	// super(icn);
	// this.uri = uri;
	// makeClickable();
	// }

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}

	public void makeClickable() {

		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent me) {
				try {
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(JHyperlinkLabel.this.uri);
					} else {
						String os = System.getProperty("os.name").toLowerCase();

						if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0)
							Runtime.getRuntime().exec("x-www-browser " + uri);
						else if (os.indexOf("win") >= 0)
							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + uri);
						if (os.indexOf("mac") >= 0)
							Runtime.getRuntime().exec("open " + uri);
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			public void mouseReleased(MouseEvent me) {
			}

			public void mousePressed(MouseEvent me) {
			}

			public void mouseEntered(MouseEvent me) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent me) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
	}

}
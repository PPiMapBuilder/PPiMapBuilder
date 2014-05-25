package ch.picard.ppimapbuilder.ui.util;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.JLabel;

import org.cytoscape.util.swing.OpenBrowser;

@SuppressWarnings("serial")
public class JHyperlinkLabel extends JLabel {

	private URI uri;
	private OpenBrowser openBrowser;

	public JHyperlinkLabel(OpenBrowser openBrowser) {
		super();
	}

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

						if (os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0)
							Runtime.getRuntime().exec("x-www-browser " + uri);
						else if (os.contains("win"))
							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + uri);
						if (os.contains("mac"))
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
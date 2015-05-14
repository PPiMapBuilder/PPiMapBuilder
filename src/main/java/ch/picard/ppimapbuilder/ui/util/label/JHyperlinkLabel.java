/*
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 *
 */

package ch.picard.ppimapbuilder.ui.util.label;

import org.cytoscape.util.swing.OpenBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

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
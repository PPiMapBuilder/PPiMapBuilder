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
import java.net.URI;
import java.net.URISyntaxException;

public class InParanoidLogo extends JHyperlinkLabel {

	public InParanoidLogo(OpenBrowser openBrowser) {
		super(openBrowser);

		try {
			setIcon(new ImageIcon(InParanoidLogo.class.getResource("InParanoidLogo.png")));
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

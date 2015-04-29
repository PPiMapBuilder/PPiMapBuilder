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
    
package ch.picard.ppimapbuilder.ui.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;

import org.cytoscape.util.swing.OpenBrowser;


/**
 * PicarLab logo icon label
 */
public class PicardLabIcon extends JHyperlinkLabel{
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
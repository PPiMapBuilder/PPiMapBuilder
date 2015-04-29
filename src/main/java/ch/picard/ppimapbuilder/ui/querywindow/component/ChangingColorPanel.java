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
    
package ch.picard.ppimapbuilder.ui.querywindow.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ChangingColorPanel extends JPanel implements ChangingColorComponent {

	private static final long serialVersionUID = 1L;

	private final Color focusColor;
	private final Color blurColor;
	private final Border focusBorder;
	private final Border blurBorder;

	public ChangingColorPanel(ChangingColorComponentUpdater updater, Color focusColor, Color blurColor, Border focusBorder, Border blurBorder) {
		updater.add(this);
		this.focusColor = focusColor;
		this.blurColor = blurColor;
		this.focusBorder = focusBorder;
		this.blurBorder = blurBorder;
		focusColorChange();
	}

	@Override
	public void focusColorChange() {
		boolean repaint = false;
		if (focusBorder != null) {
			repaint = true;
			setBorder(focusBorder);
		}
		if (focusColor != null) {
			repaint = true;
			setBackground(focusColor);
		}
		if (repaint)
			repaint();
	}

	@Override
	public void blurColorChange() {
		boolean repaint = false;
		if (blurBorder != null) {
			repaint = true;
			setBorder(blurBorder);
		}
		if (blurColor != null) {
			repaint = true;
			setBackground(blurColor);
		}
		if (repaint)
			repaint();
	}
}

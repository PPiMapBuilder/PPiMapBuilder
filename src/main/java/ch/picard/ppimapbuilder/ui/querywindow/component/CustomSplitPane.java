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

import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class CustomSplitPane extends JSplitPane implements ChangingColorComponent {

	private static final long serialVersionUID = 1L;

	private final Border focusBorder;
	private final Border blurBorder;
	private final Color focusColor;
	private final Color blurColor;

	public CustomSplitPane(ChangingColorComponentUpdater updater, Color focusColor, Color blurColor) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		updater.add(this);

		setUI(new BasicSplitPaneUI(){
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {
					@Override
					public void paint(Graphics g) {
						super.paint(g);
						//g.setColor(bgColor);
						//g.fillRect(0, 0, getSize().width, getSize().height);

						Graphics2D g2d = (Graphics2D) g;
						int h = 12;
						int w = 2;
						int x = (getWidth() - w) / 2;
						int y = (getHeight() - h) / 2;
						g2d.setColor(PMBUIStyle.defaultBorderColor);
						g2d.drawOval(x, y, w, h);
					}
				};
			}
		});

		setBorder(focusBorder = new MatteBorder(5, 5, 5, 5, focusColor));
		blurBorder =  new MatteBorder(5, 5, 5, 5, blurColor);

		setBackground(this.focusColor = focusColor);
		this.blurColor = blurColor;

		setDividerSize(5);
	}

	@Override
	public void focusColorChange() {
		setBorder(focusBorder);
		setBackground(focusColor);
		repaint();
	}

	@Override
	public void blurColorChange() {
		setBorder(blurBorder);
		setBackground(blurColor);
		repaint();
	}
}

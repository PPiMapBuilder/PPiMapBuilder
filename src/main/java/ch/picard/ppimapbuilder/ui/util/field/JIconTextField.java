package ch.picard.ppimapbuilder.ui.util.field;

/*
 * Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

/**
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JIconTextField extends JTextField {

	private Icon icon;
	private final int defaultMargin = 4;

	public JIconTextField() {
		super();
		this.icon = null;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public void setIcon(URL resource) {
		this.icon = new ImageIcon(resource);
		setBorder(getBorder());
	}

	@Override
	public void setBorder(Border border) {
		if (icon != null) {
			super.setBorder(BorderFactory.createCompoundBorder(
					border,
					BorderFactory.createEmptyBorder(0, icon.getIconWidth() + defaultMargin * 2, 0, defaultMargin)
			));
		} else
			super.setBorder(border);
	}

	public Icon getIcon() {
		return this.icon;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.icon != null) {
			int y = (this.getHeight() - icon.getIconHeight()) / 2;
			icon.paintIcon(this, g, defaultMargin, y);
		}
	}

}

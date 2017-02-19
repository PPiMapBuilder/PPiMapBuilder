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

package ch.picard.ppimapbuilder.ui.util.field;

import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ListDeletableItem {

	private final JPanel listPanel;
	private final JScrollPane scrollPane;

	public ListDeletableItem() {
		scrollPane = new JScrollPane(listPanel = new JPanel());
		scrollPane.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPane.setBorder(PMBUIStyle.fancyPanelBorder);

		listPanel.setBackground(Color.white);
		listPanel.setBorder(PMBUIStyle.emptyBorder);
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
	}

	public Component getComponent() {
		return scrollPane;
	}

	public void removeAllRow() {
		listPanel.removeAll();
	}

	public void addRow(ListRow row) {
		listPanel.add(row);
	}

	public static class ListRow extends JPanel {
		public ListRow(String text, String tooltip) {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setOpaque(false);
			setBorder(new EmptyBorder(1, 5, 1, 5));
			if (tooltip != null) setToolTipText(tooltip);

			add(new JLabel(text));
			add(Box.createHorizontalGlue());
		}

		public ListRow(String text) {
			this(text, null);
		}

		public ListRow addButton(JButton button) {
			add(button);
			return this;
		}

		public ListRow addDeleteButton(ActionListener deleteListener) {
			JButton deleteOrga = new DeleteButton();
			deleteOrga.addActionListener(deleteListener);
			return addButton(deleteOrga);
		}


	}
}

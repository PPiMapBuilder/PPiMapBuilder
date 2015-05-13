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

package ch.picard.ppimapbuilder.ui.querywindow.component.panel.field;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author CORNUT, CRESSANT, DUPUIS, GRAVOUIL
 *
 */
public class ReferenceOrganismListener implements ActionListener{
	private OtherOrganismSelectionPanel otherOrganismSelectionPanel;
	private final ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel;
	private JCheckBox previous = null;

	/**
	 * Create a reference organism combobox listener with reference to its parent window
	 */
	public ReferenceOrganismListener(
			OtherOrganismSelectionPanel otherOrganismSelectionPanel,
			ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel
	) {
		this.otherOrganismSelectionPanel = otherOrganismSelectionPanel;
		this.referenceOrganismSelectionPanel = referenceOrganismSelectionPanel;
	}

	/**
	 * Select and disable checkbox corresponding to the selected element in combobox
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox select = (JComboBox)e.getSource();

		// Get the checkbox corresponding to the selected element in combobox
		JCheckBox check = null;
		for(JCheckBox c : otherOrganismSelectionPanel.getOrganisms().values())
			if(select.getSelectedItem() != null && c.getText().equals(select.getSelectedItem().toString()))
				check = c;

		if(check != null)  {
			if(previous == null)
				previous = (JCheckBox) otherOrganismSelectionPanel.getOrganisms().values().toArray()[0];

			previous.setEnabled(true);

			check.setSelected(true);
			check.setEnabled(false);

			previous = check;
		}
		referenceOrganismSelectionPanel.refreshToolTip();
	}
}
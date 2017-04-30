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

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.util.label.HelpIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class ReferenceOrganismSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JComboBox<Organism> refOrgCb;
	private final OtherOrganismSelectionPanel otherOrganismSelectionPanel;
	private ItemListener itemListener = null;

	public ReferenceOrganismSelectionPanel() {
		this(null);
	}

	public ReferenceOrganismSelectionPanel(OtherOrganismSelectionPanel otherOrganismSelectionPanel) {
		this.otherOrganismSelectionPanel = otherOrganismSelectionPanel;
		setLayout(new MigLayout("ins 0", "[grow][]", "[][grow]"));
		setOpaque(false);

		// Reference organism label
		JLabel lblReferenceOrganism = new JLabel("Reference organism:");
		add(lblReferenceOrganism);

		// Reference organism Help Icon
		JLabel lblHelpRefOrganism = new HelpIcon("Select here the organism from which the protein you entered come from");
		lblHelpRefOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblHelpRefOrganism);

		// Reference organism combobox
		refOrgCb = new JComboBox<Organism>();
		refreshToolTip();
		add(refOrgCb, "newline, growx, spanx 2");
	}

	/**
	 * Updates the database list with an list of String
	 */
	public void updateOrganisms(List<Organism> organisms) {
		refOrgCb.removeAllItems();
		for (Organism organism : organisms) {
			refOrgCb.addItem(organism);
		}
		initItemListener();
	}

	private void initItemListener() {
		if (otherOrganismSelectionPanel != null) {
			otherOrganismSelectionPanel.setDisabledOrganism(getSelectedOrganism());

			if (itemListener == null) {
				itemListener = new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							final Organism disabledOrganism = (Organism) e.getItem();
							otherOrganismSelectionPanel.setDisabledOrganism(disabledOrganism);
						}
					}
				};
				refOrgCb.addItemListener(itemListener);
			}
		}
	}

	public void refreshToolTip() {
		Object item = refOrgCb.getSelectedItem();
		if (item != null) {
			refOrgCb.setToolTipText("Taxonomy ID: " + ((Organism) item).getTaxId());
		}
	}

	/**
	 * Gets the selected reference organism in the JComboBox
	 */
	public Organism getSelectedOrganism() {
		return (Organism) refOrgCb.getSelectedItem();
	}
}

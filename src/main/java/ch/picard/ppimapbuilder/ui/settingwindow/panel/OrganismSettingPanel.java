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

package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.field.JSearchTextField;
import ch.picard.ppimapbuilder.ui.util.field.ListDeletableItem;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class OrganismSettingPanel extends JPanel implements TabContent {

	private final ListDeletableItem panSourceOrganism;
	private final JPanel panSearchOrganism;
	private final JSearchTextField searchBox;
	private final SettingWindow owner;

	public OrganismSettingPanel(final SettingWindow owner) {
		super(new BorderLayout());
		setName("Organisms");

		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblSourceOrganisms = new JLabel("Preferred organisms:");
		add(lblSourceOrganisms, BorderLayout.NORTH);

		this.owner = owner;
		this.panSourceOrganism = new ListDeletableItem();
		add(panSourceOrganism.getComponent());

		this.panSearchOrganism = new JPanel();
		{
			this.searchBox = new JSearchTextField(owner);
			this.searchBox.setIcon(JSearchTextField.class.getResource("search.png"));
			this.searchBox.setMinimumSize(new Dimension(200, 30));
			this.searchBox.setPreferredSize(new Dimension(200, 30));
			this.searchBox.setMaximumSize(new Dimension(200, 30));
			this.searchBox.setBorder(PMBUIStyle.fancyPanelBorder);

			this.searchBox.setSuggestWidth(150);
			this.searchBox.setPreferredSuggestSize(new Dimension(150, 50));
			this.searchBox.setMinimumSuggestSize(new Dimension(150, 50));
			this.searchBox.setMaximumSuggestSize(new Dimension(150, 50));

			this.panSearchOrganism.add(searchBox);

			JButton addOrganism = new JButton("Add");
			addOrganism.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					UserOrganismRepository.getInstance().addOrganism(searchBox.getText());
					owner.newModificationMade();
					setActive(true); //Regen UI
				}

			});
			this.panSearchOrganism.add(addOrganism);
		}

		add(panSearchOrganism, BorderLayout.SOUTH);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setActive(boolean active) {
		if (active) {
			panSourceOrganism.removeAllRow();
			for (final Organism org : UserOrganismRepository.getInstance().getOrganisms()) {
				final ListDeletableItem.ListRow listRow = new ListDeletableItem.ListRow(
						org.getScientificName(),
						"Taxonomy ID: " + org.getTaxId()
				);
				listRow.addDeleteButton(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						UserOrganismRepository.getInstance().removeOrganismExceptLastOne(org.getScientificName());
						owner.newModificationMade();
						setActive(true); //Regen UI
					}
				});
				panSourceOrganism.addRow(listRow);
			}

			ArrayList<String> data = new ArrayList<String>(InParanoidOrganismRepository.getInstance().getOrganismNames());
			for (Organism o : UserOrganismRepository.getInstance().getOrganisms()) {
				data.remove(o.getScientificName());
			}
			searchBox.setSuggestData(data);
			searchBox.setText("");

			validate();
			repaint();
		}
	}
}

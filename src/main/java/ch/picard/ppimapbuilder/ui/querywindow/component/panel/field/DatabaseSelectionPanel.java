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

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.label.HelpIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	public DatabaseSelectionPanel() {
		setLayout(new MigLayout("ins 0", "[grow][]", "[][grow]"));
		setOpaque(false);

		// Source databases label
		JLabel lblSourceDatabases = new JLabel("Source databases:");
		add(lblSourceDatabases);

		// Source databases Help Icon
		JLabel lblHelpSourceDatabase = new HelpIcon("Select here the databases from which the interactions will be retrieved");
		lblHelpSourceDatabase.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblHelpSourceDatabase);

		// Source databases scrollpane containing a panel that will contain checkbox at display
		JScrollPane scrollPaneSourceDatabases = new JScrollPane();
		scrollPaneSourceDatabases.setBorder(PMBUIStyle.fancyPanelBorder);
		scrollPaneSourceDatabases.setViewportBorder(PMBUIStyle.emptyBorder);
		add(scrollPaneSourceDatabases, "newline, grow, spanx 2");

		// Source databases panel that will contain checkbox at display
		panSourceDatabases = new JPanel();
		panSourceDatabases.setBackground(Color.white);
		panSourceDatabases.setBorder(PMBUIStyle.emptyBorder);
		scrollPaneSourceDatabases.setViewportView(panSourceDatabases);
		panSourceDatabases.setLayout(new BoxLayout(panSourceDatabases, BoxLayout.Y_AXIS));

		databases = new LinkedHashMap<PsicquicService, JCheckBox>();
	}

	/**
	 * Updates the database list with an list of String
	 */
	public void updateList(List<PsicquicService> dbs) {
		// Creation of the database list
		databases.clear();
		panSourceDatabases.removeAll();

		// Checked and active
		for (PsicquicService db : dbs) {
			if (PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(true);
				j.setSelected(true);
				databases.put(db, j);

				panSourceDatabases.add(j);
			}
		}
		// Checked and inactive
		for (PsicquicService db : dbs) {
			if (PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && !db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(false);
				j.setSelected(false);
				databases.put(db, j);

				panSourceDatabases.add(j);
			}
		}
		// Unchecked but active
		for (PsicquicService db : dbs) {
			if (!PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(true);
				j.setSelected(false);
				databases.put(db, j);

				panSourceDatabases.add(j);
			}
		}
	}

	/**
	 * Get the list of selected databases
	 */
	public List<PsicquicService> getSelectedDatabases() {
		Set<PsicquicService> databaseList = new LinkedHashSet<PsicquicService>();

		// For each entry of the database linkedHashmap
		for (Entry<PsicquicService, JCheckBox> entry : databases.entrySet()) {
			if (entry.getValue().isSelected()) // If the checkbox is selected
			{
				databaseList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return new ArrayList<PsicquicService>(databaseList);
	}

}

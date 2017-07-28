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

import ch.picard.ppimapbuilder.PPiQueryService;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DatabaseSettingPanel extends JPanel implements TabContent {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<Map, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	private final ActionListener checkBoxClicked;
	private boolean beenUpdated;

	public DatabaseSettingPanel(final SettingWindow settingWindow) {
		super(new BorderLayout());
		setName("PSICQUIC Databases");

		setBorder(new EmptyBorder(5, 5, 5, 5));
		this.databases = new LinkedHashMap<Map, JCheckBox>();

		final JLabel lblSourceDatabases = new JLabel("Preferred databases:");
		add(lblSourceDatabases, BorderLayout.NORTH);

		this.panSourceDatabases = new JPanel();
		this.panSourceDatabases.setBackground(Color.white);
		this.panSourceDatabases.setBorder(PMBUIStyle.emptyBorder);
		this.panSourceDatabases.setLayout(new BoxLayout(panSourceDatabases, BoxLayout.Y_AXIS));

		// Source databases scrollpane containing a panel that will contain checkbox at display
		final JScrollPane scrollPaneSourceDatabases = new JScrollPane(panSourceDatabases);
		scrollPaneSourceDatabases.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPaneSourceDatabases.setBorder(PMBUIStyle.fancyPanelBorder);
		add(scrollPaneSourceDatabases, BorderLayout.CENTER);

		this.checkBoxClicked = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingWindow.newModificationMade();
			}
		};
		this.beenUpdated = false;
	}

	/**
	 * Get the list of selected databases
	 */
	public List<Map> getSelectedDatabases() {
		ArrayList<Map> databaseList = new ArrayList<Map>();

		// For each entry of the database linkedHashmap
		for (Entry<Map, JCheckBox> entry : databases.entrySet()) {
			if (entry.getValue().isSelected()) { // If the checkbox is selected
				databaseList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return databaseList;
	}

	/**
	 * Update the database list
	 */
	@Override
	public void validate() {
		if (!beenUpdated) {
			try {
				// Creation of the database list
				databases.clear();
				panSourceDatabases.removeAll();

				List<Map> services = PPiQueryService.getInstance().getPsicquicServices();
				for (Map service : services) {
					String name = (String) service.get("name");

					JCheckBox j = new JCheckBox(name, true);
					j.setEnabled(true);
					j.setSelected(PMBSettings.getInstance().getDatabaseList().contains(name));
					j.addActionListener(checkBoxClicked);
					databases.put(service, j);

					panSourceDatabases.add(j);
				}

				beenUpdated = true;
			} catch (Exception e) {
			    e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
			}
		}
		super.validate();
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setActive(boolean active) {

	}
}

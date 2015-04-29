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

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseSettingPanel extends TabPanel.TabContentPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	private final ActionListener checkBoxClicked;
	private boolean beenUpdated;

	public DatabaseSettingPanel(final SettingWindow settingWindow) {
		super(new BorderLayout(), "PSICQUIC Databases");

		setBorder(new EmptyBorder(5, 5, 5, 5));
		this.databases = new LinkedHashMap<PsicquicService, JCheckBox>();

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
	public List<PsicquicService> getSelectedDatabases() {
		ArrayList<PsicquicService> databaseList = new ArrayList<PsicquicService>();

		// For each entry of the database linkedHashmap
		for (Entry<PsicquicService, JCheckBox> entry : databases.entrySet()) {
			if (entry.getValue().isSelected()) { // If the checkbox is selected
				databaseList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return databaseList;
	}

	@Override
	public void setVisible(boolean opening) {
		super.setVisible(opening);
		if (opening) {
			resetUI();
		}
	}

	/**
	 * Update the database list asynchronously
	 */
	@Override
	public synchronized void resetUI() {
		if (!beenUpdated) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					PsicquicRegistry reg = PsicquicRegistry.getInstance();

					try {
						// Creation of the database list
						databases.clear();
						panSourceDatabases.removeAll();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						for (PsicquicService db : reg.getServices()) {
							JCheckBox j = new JCheckBox(db.getName(), true);
							j.setEnabled(true);
							j.setSelected(PMBSettings.getInstance().getDatabaseList().contains(db.getName()));
							j.addActionListener(checkBoxClicked);
							databases.put(db, j);

							panSourceDatabases.add(j);
						}
						beenUpdated = true;
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
					}

					repaint();
				}
			});
		}
	}
}

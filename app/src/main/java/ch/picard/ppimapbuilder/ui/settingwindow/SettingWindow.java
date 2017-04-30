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
    
package ch.picard.ppimapbuilder.ui.settingwindow;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.settings.PMBSettingSaveTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.panel.DatabaseSettingPanel;
import ch.picard.ppimapbuilder.ui.settingwindow.panel.GOSlimSettingPanel;
import ch.picard.ppimapbuilder.ui.settingwindow.panel.OrganismSettingPanel;
import ch.picard.ppimapbuilder.ui.settingwindow.panel.OrthologySettingPanel;
import ch.picard.ppimapbuilder.ui.util.FocusPropagator;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabPanel;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Map;

/**
 * PPiMapBuilder setting window
 */
public class SettingWindow extends JDialog implements FocusListener {

	private static final long serialVersionUID = 1L;

	private final JPanel mainPanel;
	private final JPanel bottomPanel;


	private final TabPanel settingTabPanel;
	private final DatabaseSettingPanel databaseSettingPanel;
	private final OrganismSettingPanel organismSettingPanel;
	private final OrthologySettingPanel orthologySettingPanel;
	private final GOSlimSettingPanel goSlimSettingPanel;

	private final JButton saveSettings;
	private final JButton close;

	private PMBSettingSaveTaskFactory saveSettingFactory;
	private TaskManager taskManager;

	private boolean modificationMade;

	private final Border focusBorder = new CompoundBorder(
			new MatteBorder(5, 5, 5, 5, PMBUIStyle.focusActiveTabColor),
			PMBUIStyle.fancyPanelBorder
	);
	private final Border blurBorder = new CompoundBorder(
			new MatteBorder(5, 5, 5, 5, PMBUIStyle.blurActiveTabColor),
			PMBUIStyle.fancyPanelBorder
	);

	public SettingWindow(OpenBrowser openBrowser) {
		setTitle("PPiMapBuilder Settings");
		setLayout(new BorderLayout());

		final FocusPropagator focusPropagator = new FocusPropagator(this);
		addWindowFocusListener(focusPropagator);
		focusPropagator.add(this);

		{// Main panel
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			mainPanel.add(settingTabPanel = new TabPanel<TabContent>(
					databaseSettingPanel = new DatabaseSettingPanel(this),
					organismSettingPanel = new OrganismSettingPanel(this),
					orthologySettingPanel = new OrthologySettingPanel(openBrowser, this),
					goSlimSettingPanel = new GOSlimSettingPanel(this)
			));
			focusPropagator.add(settingTabPanel);
			settingTabPanel.getViewportPanel().setBorder(focusBorder);
			add(mainPanel, BorderLayout.CENTER);
		}

		{// Bottom panel
			bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			bottomPanel.setBackground(PMBUIStyle.focusActiveTabColor);

			bottomPanel.add(close = new JButton("Close"));
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}

			});

			bottomPanel.add(saveSettings = new JButton("Save"));
			saveSettings.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					saveSettings();
				}

			});

			add(bottomPanel, BorderLayout.SOUTH);
		}

		//getRootPane().setDefaultButton(saveSettings);

		setModificationMade(false);

		Dimension d = new Dimension(552, 420);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(null);
	}

	public void newModificationMade() {
		setModificationMade(true);
	}

	private void setModificationMade(boolean modificationMade) {
		saveSettings.setEnabled(this.modificationMade = modificationMade);
		getRootPane().setDefaultButton(modificationMade ? saveSettings : null);
	}

	private void saveSettings() {
		// DATABASE SETTINGS SAVE
		ArrayList<String> databases = new ArrayList<String>();
		for (Map s : databaseSettingPanel.getSelectedDatabases()) {
			databases.add((String) s.get("name"));
		}
		PMBSettings.getInstance().setDatabaseList(databases);

		// ORGANISM SETTINGS SAVE
		PMBSettings.getInstance().setOrganismList(UserOrganismRepository.getInstance().getOrganisms());

		// GO slim settings save
		PMBSettings.getInstance().setGoSlimList(GOSlimRepository.getInstance().getGOSlims());

		// SAVING TASK
		taskManager.execute(saveSettingFactory.createTaskIterator());

		setModificationMade(false);
	}

	public void setSaveSettingFactory(
			PMBSettingSaveTaskFactory saveSettingFactory) {
		this.saveSettingFactory = saveSettingFactory;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	private boolean silent = false;

	public void closeSilently() {
		silent = true;
		setVisible(false);
	}

	@Override
	public void setVisible(boolean opening) {
		if (opening) {
			settingTabPanel.getActivePanel().getComponent().validate();
		} else {
			if (!silent) {
				if (modificationMade) {
					int res = JOptionPane.showOptionDialog(
							this,
							"Are you sure you want to close the settings window without saving?",
							"Unsaved settings",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							new String[]{"Save and close", "Close", "Cancel"},
							"Save and close"
					);
					if (res == 0) saveSettings();
					else if (res == 2) return;
				}
				setModificationMade(false);

				//PMBSettings.load();
				UserOrganismRepository.resetToSettings();

				GOSlimRepository.resetToSettings();
				//this.dispose();
			}
			silent = false;
		}
		setModal(opening);
		super.setVisible(opening);
	}

	@Override
	public void focusGained(FocusEvent e) {
		settingTabPanel.getViewportPanel().setBorder(focusBorder);
		bottomPanel.setBackground(PMBUIStyle.focusActiveTabColor);
		//settingTabPanel.repaint();
	}

	@Override
	public void focusLost(FocusEvent e) {
		settingTabPanel.getViewportPanel().setBorder(blurBorder);
		bottomPanel.setBackground(PMBUIStyle.blurActiveTabColor);
		//settingTabPanel.repaint();
	}
}

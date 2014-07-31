package ch.picard.ppimapbuilder.ui.settingwindow;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.settings.PMBSettingSaveTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.panel.*;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * PPiMapBuilder setting window
 */
public class SettingWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	private final TabPanel tabPanel;
	private final DatabaseSettingPanel databaseSettingPanel;
	private final OrganismSettingPanel organismSettingPanel;
	private final OrthologySettingPanel orthologySettingPanel;
	private final GOSlimSettingPanel goSlimSettingPanel;

	private final JButton saveSettings;
	private final JButton close;

	private PMBSettingSaveTaskFactory saveSettingFactory;
	private TaskManager taskManager;
	private final OpenBrowser openBrowser;

	private boolean modificationMade;

	public SettingWindow(OpenBrowser openBrowser) {
		setTitle("PPiMapBuilder Settings");
		setLayout(new BorderLayout());

		this.openBrowser = openBrowser;

		{// Main panel
			JPanel main = new JPanel();
			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

			main.add(tabPanel = new TabPanel(
					databaseSettingPanel = new DatabaseSettingPanel(this),
					organismSettingPanel = new OrganismSettingPanel(this),
					orthologySettingPanel = new OrthologySettingPanel(openBrowser, this),
					goSlimSettingPanel = new GOSlimSettingPanel(this)
			));
			add(main, BorderLayout.CENTER);
		}

		{// Bottom panel
			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			bottom.add(close = new JButton("Close"));
			Dimension size = new Dimension(
					(int) close.getPreferredSize().getWidth() + 50,
					(int) close.getPreferredSize().getHeight()
			);
			close.setPreferredSize(size);
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}

			});

			bottom.add(saveSettings = new JButton("Save"));
			saveSettings.setPreferredSize(size);
			saveSettings.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					saveSettings();
				}

			});

			add(bottom, BorderLayout.SOUTH);
		}

		//getRootPane().setDefaultButton(saveSettings);

		setModificationMade(false);

		Dimension d = new Dimension(550, 420);
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
		saveSettings.grabFocus();
		getRootPane().setDefaultButton(modificationMade ? saveSettings : null);
	}

	private void saveSettings() {
		// DATABASE SETTINGS SAVE
		ArrayList<String> databases = new ArrayList<String>();
		for (PsicquicService s : databaseSettingPanel.getSelectedDatabases()) {
			databases.add(s.getName());
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
			((TabPanel.TabContentPanel) tabPanel.getSelectedComponent()).resetUI();
			this.toFront();
			this.requestFocus();
			this.setModal(opening);
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
			} else silent = false;
		}
		super.setVisible(opening);
	}
}

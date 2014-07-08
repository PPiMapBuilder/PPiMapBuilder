package ch.picard.ppimapbuilder.ui.settingwindow;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.settings.PMBSettingSaveTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.component.panel.*;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * PPiMapBuilder setting window
 */
public class SettingWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	private JButton saveSettings;
	private JButton close;

	private DatabaseSettingPanel databaseSettingPanel;
	private OrganismSettingPanel organismSettingPanel;
	private OrthologySettingPanel orthologySettingPanel;
	private GOSlimSettingPanel goSlimSettingPanel;

	private PMBSettingSaveTaskFactory saveSettingFactory;
	private TaskManager taskManager;
	private final OpenBrowser openBrowser;

	private boolean modificationMade;

	public SettingWindow(OpenBrowser openBrowser) {
		setTitle("PPiMapBuilder Settings");
		setLayout(new BorderLayout());

		this.openBrowser = openBrowser;

		add(initMainPanel(), BorderLayout.CENTER);
		add(initBottomPanel(), BorderLayout.SOUTH);
		//getRootPane().setDefaultButton(saveSettings);

		setModificationMade(false);

		Dimension d = new Dimension(450, 320);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(null);
	}

	public void updateLists(List<PsicquicService> dbs) {
		databaseSettingPanel.updateList(dbs);
	}

	private JPanel initMainPanel() {

		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		databaseSettingPanel = new DatabaseSettingPanel(this);
		organismSettingPanel = new OrganismSettingPanel(this);
		orthologySettingPanel = new OrthologySettingPanel(openBrowser, this);
		goSlimSettingPanel = new GOSlimSettingPanel();

		main.add(new TabPanel(
				databaseSettingPanel,
				organismSettingPanel,
				orthologySettingPanel,
				goSlimSettingPanel
		));

		return main;
	}

	public void newModificationMade() {
		setModificationMade(true);
	}

	private JPanel initBottomPanel() {
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

		return bottom;
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
		PMBSettings.getInstance().setOrganismList((ArrayList<Organism>) UserOrganismRepository.getInstance().getOrganisms());

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

	public OrganismSettingPanel getOrganismSettingPanel() {
		return organismSettingPanel;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	@Override
	public void setVisible(boolean visible) {
		//closing
		if (!visible) {
			if(modificationMade) {
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
				if(res == 0) saveSettings();
				else if(res == 2) return;
			}
			setModificationMade(false);

			PMBSettings.getInstance().readSettings();
			UserOrganismRepository.resetUserOrganismRepository();
			getOrganismSettingPanel().updatePanSourceOrganism();
			getOrganismSettingPanel().updateSuggestions();

			this.dispose();
		}

		this.setModal(visible);
		super.setVisible(visible);
	}
}

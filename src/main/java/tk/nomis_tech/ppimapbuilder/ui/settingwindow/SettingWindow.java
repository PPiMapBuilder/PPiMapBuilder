package tk.nomis_tech.ppimapbuilder.ui.settingwindow;

import org.cytoscape.work.TaskManager;

import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicService;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettingSaveTaskFactory;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel.DatabaseSettingPanel;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel.OrganismSettingPanel;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel.OrthologySettingPanel;
import tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel.TabPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * PPiMapBuilder setting window
 */
public class SettingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton saveSettings;
	private JButton cancel;

	private DatabaseSettingPanel databaseSettingPanel;
	private OrganismSettingPanel organismSettingPanel;
	private OrthologySettingPanel orthologySettingPanel;

	private PMBSettingSaveTaskFactory saveSettingFactory;
	private TaskManager taskManager;

	public SettingWindow() {
		setTitle("PPiMapBuilder Settings");
		setLayout(new BorderLayout());
		add(initMainPanel(), BorderLayout.CENTER);
		add(initBottomPanel(), BorderLayout.SOUTH);
		//getRootPane().setDefaultButton(saveSettings);

		initListeners();
		
		Dimension d = new Dimension(500, 300);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(JFrame.getFrames()[0]);
		
	}
	
	public void updateLists(List<PsicquicService> dbs) {
		databaseSettingPanel.updateList(dbs);
	}

	private JPanel initMainPanel() {

		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		databaseSettingPanel = new DatabaseSettingPanel();
		organismSettingPanel = new OrganismSettingPanel(this);
		orthologySettingPanel = new OrthologySettingPanel();

		
		TabPanel tabPanel = new TabPanel(
			databaseSettingPanel,
			organismSettingPanel,
			orthologySettingPanel
		);

		main.add(tabPanel);
		
		return main;
	}

	private JPanel initBottomPanel() {
		JPanel bottom = new JPanel(new GridLayout(1, 1));

		cancel = new JButton("Cancel");
		saveSettings = new JButton("Save");
		
		bottom.add(cancel);
		bottom.add(saveSettings);

		return bottom;
	}

	private void initListeners() {
		saveSettings.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				SettingWindow.this.setVisible(false);
				SettingWindow.this.dispose();

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
				
			}

		});
		
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				PMBSettings.getInstance().readSettings();
				UserOrganismRepository.resetUserOrganismRepository();
				getOrganismSettingPanel().updatePanSourceOrganism();
				getOrganismSettingPanel().updateSuggestions();
				
				SettingWindow.this.setVisible(false);
				SettingWindow.this.dispose();
				
			}

		});
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

}

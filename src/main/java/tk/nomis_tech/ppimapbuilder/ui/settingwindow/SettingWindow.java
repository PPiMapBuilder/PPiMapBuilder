package tk.nomis_tech.ppimapbuilder.ui.settingwindow;

import org.cytoscape.work.TaskManager;

import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicService;
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
		System.out.println("#6");
		add(initMainPanel(), BorderLayout.CENTER);

		System.out.println("#6.91");
		add(initBottomPanel(), BorderLayout.SOUTH);

		System.out.println("#6.92");
		getRootPane().setDefaultButton(saveSettings);
		System.out.println("#7");

		initListeners();

		System.out.println("#13");
		
		Dimension d = new Dimension(500, 300);
		setBounds(new Rectangle(d));
		setMinimumSize(d);
		setResizable(true);
		setLocationRelativeTo(JFrame.getFrames()[0]);
		

		System.out.println("#14");
	}
	
	public void updateLists(List<PsicquicService> dbs) {
		databaseSettingPanel.updateList(dbs);
	}

	private JPanel initMainPanel() {

		System.out.println("#6.2");
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));


		System.out.println("#6.3");
		
		databaseSettingPanel = new DatabaseSettingPanel();
		organismSettingPanel = new OrganismSettingPanel(this);
		orthologySettingPanel = new OrthologySettingPanel();


		System.out.println("#6.4");
		
		TabPanel tabPanel = new TabPanel(
			databaseSettingPanel,
			organismSettingPanel,
			orthologySettingPanel
		);
		

		System.out.println("#6.5");

		main.add(tabPanel);
		
		return main;
	}

	private JPanel initBottomPanel() {
		JPanel bottom = new JPanel(new GridLayout(1, 1));
		

		System.out.println("#6.6");

		cancel = new JButton("Cancel");
		saveSettings = new JButton("Save");


		System.out.println("#6.7");
		
		bottom.add(cancel);
		bottom.add(saveSettings);


		System.out.println("#6.8");
		return bottom;
	}

	private void initListeners() {
		System.out.println("#8");
		saveSettings.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.out.println("#9");

				SettingWindow.this.setVisible(false);
				SettingWindow.this.dispose();


				System.out.println("#10");
				// TODO: currently save the three panel at the same time so check if it is a good idea
				// DATABASE SETTINGS SAVE
				ArrayList<String> databases = new ArrayList<String>();
				for (PsicquicService s : databaseSettingPanel.getSelectedDatabases()) {
					databases.add(s.getName());
				}
				PMBSettings.getInstance().setDatabaseList(databases);
				taskManager.execute(saveSettingFactory.createTaskIterator());
				

				System.out.println("#11");
				// ORGANISM SETTINGS SAVE
				

			}

		});
		

		System.out.println("#12");
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
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

}

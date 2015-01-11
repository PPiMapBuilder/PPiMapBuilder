package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.TabableView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseSettingPanel extends JPanel implements TabContent {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	private final ActionListener checkBoxClicked;
	private boolean beenUpdated;

	public DatabaseSettingPanel(final SettingWindow settingWindow) {
		super(new BorderLayout());
		setName("PSICQUIC Databases");

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
			validate();
		}
	}

	/**
	 * Update the database list
	 */
	@Override
	public void validate() {
		if (!beenUpdated) {
			PsicquicRegistry reg = PsicquicRegistry.getInstance();

			try {
				// Creation of the database list
				databases.clear();
				panSourceDatabases.removeAll();

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
		}
		super.validate();
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
}

package tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel;

import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;
import tk.nomis_tech.ppimapbuilder.webservice.psicquic.PsicquicService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseSettingPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	public DatabaseSettingPanel() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		databases = new LinkedHashMap<PsicquicService, JCheckBox>();

		final JLabel lblSourceDatabases = new JLabel("Preferred databases:");
		add(lblSourceDatabases, BorderLayout.NORTH);

		panSourceDatabases = new JPanel();
		panSourceDatabases.setBackground(Color.white);
		panSourceDatabases.setBorder(new EmptyBorder(0, 0, 0, 0));

		panSourceDatabases.setLayout(new BoxLayout(panSourceDatabases, BoxLayout.Y_AXIS));

		// Source databases scrollpane containing a panel that will contain checkbox at display
		final JScrollPane scrollPaneSourceDatabases = new JScrollPane(panSourceDatabases);
		scrollPaneSourceDatabases.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPaneSourceDatabases, BorderLayout.CENTER);
	}

	/**
	 * Updates the database list with an list of String
	 *
	 * @param dbs
	 */
	public void updateList(List<PsicquicService> dbs) {
		// Creation of the database list
		databases.clear();
		panSourceDatabases.removeAll();
		
		for (PsicquicService db : dbs) {
			JCheckBox j = new JCheckBox(db.getName(), true);
			j.setEnabled(true);
			j.setSelected(PMBStore.getSettings().getDatabaseList().contains(db.getName()));
			databases.put(db, j);

			panSourceDatabases.add(j);
		}
	}

	/**
	 * Get the list of selected databases
	 *
	 * @return list of database values
	 */
	public List<PsicquicService> getSelectedDatabases() {
		ArrayList<PsicquicService> databaseList = new ArrayList<PsicquicService>();

		// For each entry of the database linkedHashmap
		for (Entry<PsicquicService, JCheckBox> entry : databases.entrySet()) {
			if (entry.getValue().isSelected()) // If the checkbox is selected
			{
				databaseList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return databaseList;
	}

}

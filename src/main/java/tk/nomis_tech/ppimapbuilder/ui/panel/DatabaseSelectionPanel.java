package tk.nomis_tech.ppimapbuilder.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import tk.nomis_tech.ppimapbuilder.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

public class DatabaseSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	public DatabaseSelectionPanel() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		databases = new LinkedHashMap<PsicquicService, JCheckBox>();

		final JLabel lblSourceDatabases = new JLabel("Source databases:");
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
		
		// Checked and active
		for (PsicquicService db : dbs) {
			if (PMBSettings.getDatabaseList().contains(db.getName()) && db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(true);
				j.setSelected(true);
				databases.put(db, j);
	
				panSourceDatabases.add(j);
			}
		}
		// Checked and inactive
		for (PsicquicService db : dbs) {
			if (PMBSettings.getDatabaseList().contains(db.getName()) && !db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(false);
				j.setSelected(false);
				databases.put(db, j);
	
				panSourceDatabases.add(j);
			}
		}
		// Unchecked but active
		for (PsicquicService db : dbs) {
			if (!PMBSettings.getDatabaseList().contains(db.getName()) && db.isActive()) {
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

package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.util.HelpIcon;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseSelectionPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<PsicquicService, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	public DatabaseSelectionPanel(JPanel parent, CompoundBorder fancyBorder) {
		// Source databases label
		javax.swing.JLabel lblSourceDatabases = new javax.swing.JLabel("Source databases:");
		parent.add(lblSourceDatabases, "cell 0 4");

		// Source databases Help Icon
		JLabel lblHelpSourceDatabase = new HelpIcon("Select here the databases from which the interactions will be retrieved");
		lblHelpSourceDatabase.setHorizontalAlignment(SwingConstants.RIGHT);
		parent.add(lblHelpSourceDatabase, "cell 1 4");

		// Source databases scrollpane containing a panel that will contain checkbox at display
		JScrollPane scrollPaneSourceDatabases = new JScrollPane();
		scrollPaneSourceDatabases.setBorder(fancyBorder);
		scrollPaneSourceDatabases.setViewportBorder(PMBUIStyle.emptyBorder);
		parent.add(scrollPaneSourceDatabases, "cell 0 5 2 1,grow");

		// Source databases panel that will contain checkbox at display
		panSourceDatabases = new JPanel();
		panSourceDatabases.setBackground(Color.white);
		panSourceDatabases.setBorder(PMBUIStyle.emptyBorder);
		scrollPaneSourceDatabases.setViewportView(panSourceDatabases);
		panSourceDatabases.setLayout(new BoxLayout(panSourceDatabases, BoxLayout.Y_AXIS));
		
		databases = new LinkedHashMap<PsicquicService, JCheckBox>();
	}

	/**
	 * Updates the database list with an list of String
	 */
	public void updateList(List<PsicquicService> dbs) {
		// Creation of the database list
		databases.clear();
		panSourceDatabases.removeAll();
		
		// Checked and active
		for (PsicquicService db : dbs) {
			if (PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(true);
				j.setSelected(true);
				databases.put(db, j);
	
				panSourceDatabases.add(j);
			}
		}
		// Checked and inactive
		for (PsicquicService db : dbs) {
			if (PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && !db.isActive()) {
				JCheckBox j = new JCheckBox(db.getName(), true);
				j.setEnabled(false);
				j.setSelected(false);
				databases.put(db, j);
	
				panSourceDatabases.add(j);
			}
		}
		// Unchecked but active
		for (PsicquicService db : dbs) {
			if (!PMBSettings.getInstance().getDatabaseList().contains(db.getName()) && db.isActive()) {
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

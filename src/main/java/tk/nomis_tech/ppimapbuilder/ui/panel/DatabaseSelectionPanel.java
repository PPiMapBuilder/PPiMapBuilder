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

import org.hupo.psi.mi.psicquic.registry.ServiceType;

public class DatabaseSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<ServiceType, JCheckBox> databases;
	private final JPanel panSourceDatabases;

	public DatabaseSelectionPanel() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		databases = new LinkedHashMap<ServiceType, JCheckBox>();
		
		final JLabel lblSourceDatabases = new JLabel("Source databases:");
		add(lblSourceDatabases, BorderLayout.NORTH);

		panSourceDatabases = new JPanel();
		panSourceDatabases.setBackground(Color.white);
		panSourceDatabases.setBorder(new EmptyBorder(0, 0, 0, 0));
		//scrollPaneSourceDatabases.setViewportView(panSourceDatabases);
		panSourceDatabases.setLayout(new BoxLayout(panSourceDatabases, BoxLayout.Y_AXIS));
		
		// Source databases scrollpane containing a panel that will contain checkbox at display
		final JScrollPane scrollPaneSourceDatabases = new JScrollPane(panSourceDatabases);
		scrollPaneSourceDatabases.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPaneSourceDatabases, BorderLayout.CENTER);
	}
	
	/**
	 * Updates the database list with an list of String
	 * @param dbs
	 */
	public void updateList(List<ServiceType> dbs) {
		// Creation of the database list
		databases.clear();
		panSourceDatabases.removeAll();
		for (ServiceType db : dbs) {
			JCheckBox j = new JCheckBox(db.getName(), true);
			j.setBackground(Color.white);
			databases.put(db, j);
			
			JPanel l = new JPanel(new BorderLayout());
			l.setOpaque(false);
			l.add(j, BorderLayout.CENTER);
			l.add(new JLabel(db.getTags().toString().replaceAll("[\\[\\]]", "")), BorderLayout.EAST);
			panSourceDatabases.add(l);
		}
	}
	
	/**
	 * Get the list of selected databases
	 * @return list of database values
	 */
	public List<ServiceType> getSelectedDatabases() {
		ArrayList<ServiceType> databaseList = new ArrayList<ServiceType>();
		
		// For each entry of the database linkedHashmap
		for (Entry<ServiceType, JCheckBox> entry : databases.entrySet())
			if (entry.getValue().isSelected()) // If the checkbox is selected
				databaseList.add(entry.getKey()); // The database name is add into the list to be returned
		
		return databaseList;
	}
	
}

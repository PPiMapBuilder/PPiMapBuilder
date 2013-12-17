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

import org.cytoscape.service.util.internal.CyActivator;

import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

public class OtherOrganismSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<Organism, JCheckBox> organisms;
	private final JPanel panSourceOtherOrganisms;

	public OtherOrganismSelectionPanel() {

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		organisms = new LinkedHashMap<Organism, JCheckBox>();
		final JLabel lblSourceDatabases = new JLabel("Organisms for homology:");
		add(lblSourceDatabases, BorderLayout.NORTH);

		panSourceOtherOrganisms = new JPanel();
		panSourceOtherOrganisms.setBackground(Color.white);
		panSourceOtherOrganisms.setBorder(new EmptyBorder(0, 0, 0, 0));

		panSourceOtherOrganisms.setLayout(new BoxLayout(panSourceOtherOrganisms, BoxLayout.Y_AXIS));

		// Source databases scrollpane containing a panel that will contain checkbox at display
		final JScrollPane scrollPaneSourceDatabases = new JScrollPane(panSourceOtherOrganisms);
		scrollPaneSourceDatabases.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		add(scrollPaneSourceDatabases, BorderLayout.CENTER);
	}

	/**
	 * Updates the database list with an list of String
 	 * Updates the organism list with an list of organism
	 * @param ogs
	 */
	public void updateList(List<Organism> ogs) {
		// Creation of the database list
		organisms.clear();
		panSourceOtherOrganisms.removeAll();
		for (Organism og : ogs) {
			JCheckBox j = new JCheckBox(og.getName(), true);
			j.setBackground(Color.white);
			organisms.put(og, j);

			panSourceOtherOrganisms.add(j);
		}
	}

	/**
	 * Get the list of selected databases
	 *
	 * @return list of database values
	 */
	public List<Organism> getSelectedOrganisms() {
		ArrayList<Organism> organismList = new ArrayList<Organism>();

		// For each entry of the database linkedHashmap
		for (Entry<Organism, JCheckBox> entry : organisms.entrySet()) {
			if (entry.getValue().isSelected()) // If the checkbox is selected
			{
				organismList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return organismList;
	}

}

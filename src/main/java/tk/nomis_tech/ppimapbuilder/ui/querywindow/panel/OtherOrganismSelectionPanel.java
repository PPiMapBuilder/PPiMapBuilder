package tk.nomis_tech.ppimapbuilder.ui.querywindow.panel;

import tk.nomis_tech.ppimapbuilder.ui.util.HelpIcon;
import tk.nomis_tech.ppimapbuilder.data.store.Organism;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class OtherOrganismSelectionPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<Organism, JCheckBox> organisms;
	private final JPanel panSourceOtherOrganisms;

	public OtherOrganismSelectionPanel(JPanel parent, Color darkForeground, CompoundBorder panelBorder, CompoundBorder fancyBorder) {

		//parent.setLayout(new MigLayout());
		
		// Other organisms label
		JLabel lblHomologOrganism = new JLabel("Other organisms:");
		parent.add(lblHomologOrganism, "cell 0 2");

		// Other organisms Help Icon
		JLabel lblHelpOtherOrganism = new HelpIcon("Select here the other organism in which you want to search homologous interactions");
		lblHelpOtherOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		parent.add(lblHelpOtherOrganism, "cell 1 2");

		// Other organisms scrollpane containing a panel that will contain checkbox at display
		JScrollPane scrollPaneOtherOrganisms = new JScrollPane();
		scrollPaneOtherOrganisms.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPaneOtherOrganisms.setBorder(fancyBorder);
		parent.add(scrollPaneOtherOrganisms, "cell 0 3 2 1,grow");

		// Other organisms panel that will contain checkbox at display
		panSourceOtherOrganisms = new JPanel();
		panSourceOtherOrganisms.setBorder(new EmptyBorder(0, 0, 0, 0));
		panSourceOtherOrganisms.setBackground(Color.WHITE);
		scrollPaneOtherOrganisms.setViewportView(panSourceOtherOrganisms);
		panSourceOtherOrganisms.setLayout(new BoxLayout(panSourceOtherOrganisms,BoxLayout.Y_AXIS));
		
		organisms = new LinkedHashMap<Organism, JCheckBox>();
		
		/*setLayout(new BorderLayout());
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
		add(scrollPaneSourceDatabases, BorderLayout.CENTER);*/
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
			JCheckBox j = new JCheckBox(og.getCommonName(), true);
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
	
	public LinkedHashMap<Organism, JCheckBox> getOrganisms() {
		return organisms;
	}

}

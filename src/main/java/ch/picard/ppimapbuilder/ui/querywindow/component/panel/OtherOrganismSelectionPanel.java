package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.util.HelpIcon;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class OtherOrganismSelectionPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<Organism, JCheckBox> organisms;
	private final JPanel panSourceOtherOrganisms;

	public OtherOrganismSelectionPanel(JPanel parent, CompoundBorder fancyBorder) {
		// Other organisms label
		JLabel lblHomologOrganism = new JLabel("Other organisms:");
		parent.add(lblHomologOrganism, "cell 0 2");

		// Other organisms Help Icon
		JLabel lblHelpOtherOrganism = new HelpIcon("Select here the other organism in which you want to search homologous interactions");
		lblHelpOtherOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		parent.add(lblHelpOtherOrganism, "cell 1 2");

		// Other organisms scrollpane containing a panel that will contain checkbox at display
		JScrollPane scrollPaneOtherOrganisms = new JScrollPane();
		scrollPaneOtherOrganisms.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPaneOtherOrganisms.setBorder(fancyBorder);
		parent.add(scrollPaneOtherOrganisms, "cell 0 3 2 1,grow");

		// Other organisms panel that will contain checkbox at display
		panSourceOtherOrganisms = new JPanel();
		panSourceOtherOrganisms.setBorder(PMBUIStyle.emptyBorder);
		panSourceOtherOrganisms.setBackground(Color.WHITE);
		scrollPaneOtherOrganisms.setViewportView(panSourceOtherOrganisms);
		panSourceOtherOrganisms.setLayout(new BoxLayout(panSourceOtherOrganisms,BoxLayout.Y_AXIS));
		
		organisms = new LinkedHashMap<Organism, JCheckBox>();
	}

	/**
	 * Updates the database list with an list of String
 	 * Updates the organism list with an list of organism
	 */
	public void updateList(List<Organism> organisms) {
		// Creation of the database list
		this.organisms.clear();
		panSourceOtherOrganisms.removeAll();
		for (Organism organism : organisms) {
			JCheckBox checkBox = new JCheckBox(organism.getScientificName(), true);
			checkBox.setToolTipText("Taxonomy ID: " + organism.getTaxId());
			checkBox.setBackground(Color.white);
			this.organisms.put(organism, checkBox);

			panSourceOtherOrganisms.add(checkBox);
		}
	}

	/**
	 * Get the list of selected databases
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
package ch.picard.ppimapbuilder.ui.querywindow.component.panel.field;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.util.label.HelpIcon;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class OtherOrganismSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<Organism, JCheckBox> organisms;
	private final JPanel panSourceOtherOrganisms;

	public OtherOrganismSelectionPanel() {
		setLayout(new MigLayout("ins 0", "[grow][]", "[][grow]"));
		setOpaque(false);

		// Other organisms label
		JLabel lblHomologOrganism = new JLabel("Other organisms:");
		add(lblHomologOrganism);

		// Other organisms Help Icon
		JLabel lblHelpOtherOrganism = new HelpIcon("Select here the other organism in which you want to search homologous interactions");
		lblHelpOtherOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblHelpOtherOrganism);

		// Other organisms scrollpane containing a panel that will contain checkbox at display
		JScrollPane scrollPaneOtherOrganisms = new JScrollPane();
		scrollPaneOtherOrganisms.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPaneOtherOrganisms.setBorder(PMBUIStyle.fancyPanelBorder);
		add(scrollPaneOtherOrganisms, "newline, grow, spanx 2");

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
		Set<Organism> organismList = new LinkedHashSet<Organism>();

		// For each entry of the database linkedHashmap
		for (Entry<Organism, JCheckBox> entry : organisms.entrySet()) {
			if (entry.getValue().isSelected()) // If the checkbox is selected
			{
				organismList.add(entry.getKey()); // The database name is add into the list to be returned
			}
		}
		return new ArrayList<Organism>(organismList);
	}

	public LinkedHashMap<Organism, JCheckBox> getOrganisms() {
		return organisms;
	}

}

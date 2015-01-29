package ch.picard.ppimapbuilder.ui.querywindow.component.panel.field;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.util.label.HelpIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class ReferenceOrganismSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JComboBox<Organism> refOrgCb;

	public ReferenceOrganismSelectionPanel() {
		this(null);
	}

	public ReferenceOrganismSelectionPanel(
			OtherOrganismSelectionPanel otherOrganismSelectionPanel
	) {
		setLayout(new MigLayout("ins 0", "[grow][]", "[][grow]"));
		setOpaque(false);

		// Reference organism label
		JLabel lblReferenceOrganism = new JLabel("Reference organism:");
		add(lblReferenceOrganism);

		// Reference organism Help Icon
		JLabel lblHelpRefOrganism = new HelpIcon("Select here the organism from which the protein you entered come from");
		lblHelpRefOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblHelpRefOrganism);

		// Reference organism combobox
		refOrgCb = new JComboBox<Organism>();
		if(otherOrganismSelectionPanel != null)
			refOrgCb.addActionListener(new ReferenceOrganismListener(otherOrganismSelectionPanel, this));
		refreshToolTip();
		add(refOrgCb, "newline, growx, spanx 2");
	}

	/**
	 * Updates the database list with an list of String
	 */
	public void updateList(List<Organism> organisms) {
		refOrgCb.removeAllItems();
		for (Organism organism : organisms)
			refOrgCb.addItem(organism);
	}

	public void refreshToolTip() {
		Object item = refOrgCb.getSelectedItem();
		if(item != null)
			refOrgCb.setToolTipText("Taxonomy ID: "+((Organism)item).getTaxId());
	}

	/**
	 * Gets the selected reference organism in the JComboBox
	 */
	public Organism getSelectedOrganism() {
		return (Organism) refOrgCb.getSelectedItem();
	}
}

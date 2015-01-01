package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.ui.querywindow.component.listener.ReferenceOrganismListener;
import ch.picard.ppimapbuilder.ui.util.HelpIcon;
import ch.picard.ppimapbuilder.data.organism.Organism;

import javax.swing.*;
import java.util.List;

public class ReferenceOrganismSelectionPanel {

	private static final long serialVersionUID = 1L;
	private final JComboBox<Organism> refOrgCb;

	public ReferenceOrganismSelectionPanel(QueryWindow parentWindow, JPanel parent) {
		//this.setLayout(new MigLayout());
		
		// Reference organism label
		JLabel lblReferenceOrganism = new JLabel("Reference organism:");
		parent.add(lblReferenceOrganism, "cell 0 0");

		// Reference organism combobox
		refOrgCb = new JComboBox<Organism>();
		refOrgCb.addActionListener(new ReferenceOrganismListener(parentWindow, this));
		refreshToolTip();

		// Reference organism Help Icon
		JLabel lblHelpRefOrganism = new HelpIcon("Select here the organism from which the protein you entered come from");
		lblHelpRefOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		parent.add(lblHelpRefOrganism, "cell 1 0");
		parent.add(refOrgCb, "cell 0 1 2 1,growx");
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

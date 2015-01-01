package ch.picard.ppimapbuilder.ui.querywindow.component.listener;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.ReferenceOrganismSelectionPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @author CORNUT, CRESSANT, DUPUIS, GRAVOUIL
 *
 */
public class ReferenceOrganismListener implements ActionListener{
	private QueryWindow createNetwork;
	private final ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel;
	private JCheckBox previous = null;

	/**
	 * Create a reference organism combobox listener with reference to its parent window
	 */
	public ReferenceOrganismListener(QueryWindow createNetwork, ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel) {
		this.createNetwork = createNetwork;
		this.referenceOrganismSelectionPanel = referenceOrganismSelectionPanel;
	}

	/**
	 * Select and disable checkbox corresponding to the selected element in combobox
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox select = (JComboBox)e.getSource();

		// Get the checkbox corresponding to the selected element in combobox
		JCheckBox check = null;
		for(JCheckBox c : createNetwork.getOtherOrganismSelectionPanel().getOrganisms().values())
			if(select.getSelectedItem() != null && c.getText().equals(select.getSelectedItem().toString()))
				check = c;

		if(check != null)  {
			if(previous == null)
				previous = (JCheckBox) createNetwork.getOtherOrganismSelectionPanel().getOrganisms().values().toArray()[0];

			previous.setEnabled(true);

			check.setSelected(true);
			check.setEnabled(false);

			previous = check;
		}
		referenceOrganismSelectionPanel.refreshToolTip();
	}
}
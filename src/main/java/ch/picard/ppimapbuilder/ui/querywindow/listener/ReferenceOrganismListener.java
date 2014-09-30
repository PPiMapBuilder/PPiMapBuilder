package ch.picard.ppimapbuilder.ui.querywindow.listener;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;

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
	private JCheckBox previous = null;

	/**
	 * Create a reference organism combobox listener with reference to its parent window
	 */
	public ReferenceOrganismListener(QueryWindow createNetwork) {
		this.createNetwork = createNetwork;
	}

	/**
	 * Select and disable checkbox corresponding to the selected element in combobox
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox select = (JComboBox)e.getSource();

		JCheckBox check = null;

		// Get the checkbox corresponding to the selected element in combobox
		for(JCheckBox c : createNetwork.getOtherOrganismSelectionPanel().getOrganisms().values())
			if(c.getText().equals(select.getSelectedItem()))
				check = c;

		if(check != null)  {
			if(previous == null)
				previous = (JCheckBox) createNetwork.getOtherOrganismSelectionPanel().getOrganisms().values().toArray()[0];

			previous.setEnabled(true);

			check.setSelected(true);
			check.setEnabled(false);

			previous = check;
		}
	}
}
package ch.picard.ppimapbuilder.ui.querywindow.listener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;


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
	 * @param window
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
		for(JCheckBox c : createNetwork.getOgs().getOrganisms().values())
			if(c.getText().equals(select.getSelectedItem()))
				check = c;

		if(check != null)  {
			if(previous == null)
				previous = (JCheckBox) createNetwork.getOgs().getOrganisms().values().toArray()[0];

			previous.setEnabled(true);

			check.setSelected(true);
			check.setEnabled(false);

			previous = check;
		}
	}
}
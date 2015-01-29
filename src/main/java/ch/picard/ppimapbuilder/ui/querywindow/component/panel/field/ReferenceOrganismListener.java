package ch.picard.ppimapbuilder.ui.querywindow.component.panel.field;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author CORNUT, CRESSANT, DUPUIS, GRAVOUIL
 *
 */
public class ReferenceOrganismListener implements ActionListener{
	private OtherOrganismSelectionPanel otherOrganismSelectionPanel;
	private final ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel;
	private JCheckBox previous = null;

	/**
	 * Create a reference organism combobox listener with reference to its parent window
	 */
	public ReferenceOrganismListener(
			OtherOrganismSelectionPanel otherOrganismSelectionPanel,
			ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel
	) {
		this.otherOrganismSelectionPanel = otherOrganismSelectionPanel;
		this.referenceOrganismSelectionPanel = referenceOrganismSelectionPanel;
	}

	/**
	 * Select and disable checkbox corresponding to the selected element in combobox
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox select = (JComboBox)e.getSource();

		// Get the checkbox corresponding to the selected element in combobox
		JCheckBox check = null;
		for(JCheckBox c : otherOrganismSelectionPanel.getOrganisms().values())
			if(select.getSelectedItem() != null && c.getText().equals(select.getSelectedItem().toString()))
				check = c;

		if(check != null)  {
			if(previous == null)
				previous = (JCheckBox) otherOrganismSelectionPanel.getOrganisms().values().toArray()[0];

			previous.setEnabled(true);

			check.setSelected(true);
			check.setEnabled(false);

			previous = check;
		}
		referenceOrganismSelectionPanel.refreshToolTip();
	}
}
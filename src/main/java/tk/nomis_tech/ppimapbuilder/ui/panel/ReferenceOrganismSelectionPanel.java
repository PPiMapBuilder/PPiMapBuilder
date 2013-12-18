package tk.nomis_tech.ppimapbuilder.ui.panel;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import tk.nomis_tech.ppimapbuilder.util.Organism;

public class ReferenceOrganismSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private List<Organism> organisms;
	private final JComboBox refOrgCb;
	private final DefaultComboBoxModel refOrgCbModel;

	public ReferenceOrganismSelectionPanel() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel title = new JLabel("Reference organism:");
		add(title, BorderLayout.NORTH);
		
		refOrgCbModel = new DefaultComboBoxModel();
		refOrgCb = new JComboBox(refOrgCbModel);
		add(refOrgCb, BorderLayout.CENTER);
	}
	
	/**
	 * Updates the database list with an list of String
	 * @param orgs
	 */
	public void updateList(List<Organism> orgs) {
		// Creation of the database list
		organisms = orgs;
		
		refOrgCbModel.removeAllElements();
		for (Organism org : orgs) {
			refOrgCbModel.addElement(org.getName());
		}
	}
	
	/**
	 * Gets the selected reference organism in the JComboBox
	 * @return
	 */
	public Organism getSelectedOrganism() {
		return organisms.get(refOrgCb.getSelectedIndex());
	}
}

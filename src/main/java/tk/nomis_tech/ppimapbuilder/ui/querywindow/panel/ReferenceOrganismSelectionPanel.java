package tk.nomis_tech.ppimapbuilder.ui.querywindow.panel;

import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.listener.ReferenceOrganismListener;
import tk.nomis_tech.ppimapbuilder.ui.util.HelpIcon;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;

import javax.swing.*;
import java.util.List;

public class ReferenceOrganismSelectionPanel {

	private static final long serialVersionUID = 1L;
	private QueryWindow parent;
	private List<Organism> organisms;
	private final JComboBox refOrgCb;
	private final DefaultComboBoxModel refOrgCbModel;

	public ReferenceOrganismSelectionPanel(QueryWindow parentWindow, JPanel parent) {
		//this.setLayout(new MigLayout());
		
		// Reference organism label
		JLabel lblReferenceOrganism = new JLabel("Reference organism:");
		parent.add(lblReferenceOrganism, "cell 0 0");

		// Reference organism combobox
		refOrgCbModel = new DefaultComboBoxModel();
		refOrgCb = new JComboBox(refOrgCbModel);
		refOrgCb.addActionListener(new ReferenceOrganismListener(parentWindow));

		// Reference organism Help Icon
		JLabel lblHelpRefOrganism = new HelpIcon("Select here the organism from which the protein you entered come from");
		lblHelpRefOrganism.setHorizontalAlignment(SwingConstants.RIGHT);
		parent.add(lblHelpRefOrganism, "cell 1 0");
		parent.add(refOrgCb, "cell 0 1 2 1,growx");
		
		
		
		/*setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel title = new JLabel("Reference organism:");
		add(title, BorderLayout.NORTH);
		
		refOrgCbModel = new DefaultComboBoxModel();
		refOrgCb = new JComboBox(refOrgCbModel);
		add(refOrgCb, BorderLayout.CENTER);*/
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
			refOrgCbModel.addElement(org.getSimpleScientificName());
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

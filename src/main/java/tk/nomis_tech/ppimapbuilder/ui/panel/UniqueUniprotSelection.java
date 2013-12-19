package tk.nomis_tech.ppimapbuilder.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Kevin Gravouil
 */
public class UniqueUniprotSelection extends JPanel {

	private ArrayList<String> idList;

	private final JPanel panIdSelect;
	private JTextField txtIds;

	public JPanel getPanIdSelect() {
		return panIdSelect;
	}

	public JTextField getTxtIds() {
		return txtIds;
	}

	public UniqueUniprotSelection() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));

		idList = new ArrayList<String>();

		final JLabel lblIdSelection = new JLabel("Uniprot accession number:");
		// uniprot pattern = ^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])$
		add(lblIdSelection, BorderLayout.NORTH);

		this.txtIds = new JTextField();
		add(txtIds, BorderLayout.CENTER);

		panIdSelect = new JPanel();
		panIdSelect.setBackground(Color.white);
		panIdSelect.setBorder(new EmptyBorder(0, 0, 0, 0));
		panIdSelect.setLayout(new BoxLayout(panIdSelect, BoxLayout.Y_AXIS));
	}

	public String getSelectedUniprotID() {
		return this.txtIds.getText();
	}
}

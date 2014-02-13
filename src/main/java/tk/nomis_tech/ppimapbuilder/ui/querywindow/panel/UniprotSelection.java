package tk.nomis_tech.ppimapbuilder.ui.querywindow.panel;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class UniprotSelection extends JPanel {

	private JTextArea identifiers;

	public ArrayList<String> getIdentifers() {
		ArrayList<String> identifierList = new ArrayList<String>();
		for (String str : identifiers.getText().split("\n")) {
			if (!str.equals("")) {
				identifierList.add(str.trim());
			}
		}
		return identifierList;
	}

	public UniprotSelection() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblIdSelection = new JLabel("Uniprot accession number:");
		// uniprot pattern = ^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])$
		add(lblIdSelection, BorderLayout.NORTH);

		this.identifiers = new JTextArea("", 5, 10);

		JScrollPane scroll = new JScrollPane(identifiers);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scroll, BorderLayout.CENTER);
	}

}

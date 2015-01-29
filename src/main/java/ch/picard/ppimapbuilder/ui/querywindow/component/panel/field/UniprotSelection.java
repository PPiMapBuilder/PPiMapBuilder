package ch.picard.ppimapbuilder.ui.querywindow.component.panel.field;

import ch.picard.ppimapbuilder.ui.util.label.HelpIcon;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class UniprotSelection extends JPanel {

	private JTextArea identifiers;

	public UniprotSelection() {
		// Uniprot identifiers left panel
		setBorder(PMBUIStyle.fancyPanelBorder);
		this.setLayout(new MigLayout("inset 15", "[129px,grow][14px:14px:14px,right]", "[20px][366px,grow][35px]"));

		// Label "Uniprot identifiers"
		JLabel lblIdentifiers = new JLabel("Uniprot Identifiers\n");
		this.add(lblIdentifiers, "flowx,cell 0 0,alignx left,aligny top");

		// Uniprot identifiers Help Icon
		JLabel lblHelpUniprotIdentifiers = new HelpIcon("Please enter Uniprot identifiers (one per line)");
		lblHelpUniprotIdentifiers.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(lblHelpUniprotIdentifiers, "cell 1 0");

		// Text area uniprot identifiers
		identifiers = new JTextArea();
		identifiers.setFont(new Font("Monospaced", Font.PLAIN, 13));
		identifiers.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Scroll pane around the text area
		JScrollPane scrollPane = new JScrollPane(identifiers);
		scrollPane.setViewportBorder(PMBUIStyle.emptyBorder);
		scrollPane.setBorder(PMBUIStyle.fancyPanelBorder);
		this.add(scrollPane, "cell 0 1 2 1,grow");

		// Cancel button
		JButton button = new JButton("Clear");
		button.setMnemonic(KeyEvent.VK_CLEAR);
		button.setAlignmentX(1.0f);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				identifiers.setText("");
			}
		});
		this.add(button, "cell 0 2 2 1,alignx right,aligny center");
	}

	public ArrayList<String> getIdentifiers() {
		ArrayList<String> identifierList = new ArrayList<String>();
		for (String str : identifiers.getText().split("\n")) {
			if (!str.equals("")) {
				identifierList.add(str.trim());
			}
		}
		return identifierList;
	}

}

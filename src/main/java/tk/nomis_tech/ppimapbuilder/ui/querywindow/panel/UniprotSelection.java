package tk.nomis_tech.ppimapbuilder.ui.querywindow.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import net.miginfocom.swing.MigLayout;
import tk.nomis_tech.ppimapbuilder.ui.util.HelpIcon;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;

import java.awt.Color;

import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.BoxLayout;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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

	public UniprotSelection(Color darkForeground, CompoundBorder panelBorder, CompoundBorder fancyBorder) {
		// Uniprot identifiers left panel
		super();
		this.setBorder(new CompoundBorder(new MatteBorder(0, 5, 0, 0, (Color) darkForeground), panelBorder));
		this.setLayout(new MigLayout("inset 10", "[129px,grow][14px:14px:14px,right]", "[20px][366px,grow][35px]"));

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
		scrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setBorder(fancyBorder);
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
		
		/*setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblIdSelection = new JLabel("Uniprot accession number:");
		// uniprot pattern = ^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])$
		add(lblIdSelection, BorderLayout.NORTH);

		this.identifiers = new JTextArea("", 5, 10);

		JScrollPane scroll = new JScrollPane(identifiers);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scroll, BorderLayout.CENTER);*/
	}

}

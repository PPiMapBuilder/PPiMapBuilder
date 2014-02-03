package tk.nomis_tech.ppimapbuilder.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class ResultPanel extends javax.swing.JPanel {

	/**
	 * Creates new form ResultPanel
	 */

	ImageIcon urlIcon = new ImageIcon(getClass().getResource("external_link_icon.gif"));
	private final JLabel lblReviewed = new JLabel(new ImageIcon(getClass().getResource("star.png")));
	private final JLabel lblUnreviewed = new JLabel(new ImageIcon(getClass().getResource("unstar.png")));

	public ResultPanel() {
		setBorder(null);
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(2000);
		add(scrollPane, BorderLayout.CENTER);

		final JPanel panel = new JPanel();
		panel.setBorder(null);
		scrollPane.setViewportView(panel);
		panel.setLayout(new MigLayout("hidemode 3", "[70px:70px:70px,grow,right][grow][]", "[][][][][][::50px,grow][30px:80px,grow 66][30px:80px,grow]"));

		JLabel lblPtnname = new JLabel("Catalase");
		lblPtnname.setBorder(new EmptyBorder(3, 3, 3, 3));
		lblPtnname.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel.add(lblPtnname, "cell 0 0 2 1,grow");

		JLabel lblUniprotId = new JLabel("Uniprot ID:");
		panel.add(lblUniprotId, "cell 0 1,alignx left");

		panel.add(lblReviewed, "cell 2 0,alignx right,aligny center");

		JLabel lblExtLink1 = new JLabel(urlIcon);
		lblExtLink1.setToolTipText("Access external link");
		panel.add(lblExtLink1, "cell 2 1,alignx right,aligny center");

		JLabel lblExtLink2 = new JLabel(urlIcon);
		lblExtLink2.setToolTipText("Access external link");
		panel.add(lblExtLink2, "cell 2 2,alignx right,aligny center");

		JLabel lblExtLink3 = new JLabel(urlIcon);
		lblExtLink3.setToolTipText("Access external link");
		panel.add(lblExtLink3, "cell 2 3,alignx right,aligny center");

		JLabel lblExtLink4 = new JLabel(urlIcon);
		lblExtLink4.setToolTipText("Access external link");
		panel.add(lblExtLink4, "cell 2 4,alignx right,aligny center");

		JLabel lblEcNumber = new JLabel("EC Number:");
		panel.add(lblEcNumber, "flowx,cell 0 2,alignx left");

		JLabel label_1 = new JLabel("1.11.1.6");
		panel.add(label_1, "cell 1 2");

		JLabel label = new JLabel();
		panel.add(label, "cell 1 2");

		JLabel lblNewLabel = new JLabel("Organism:");
		panel.add(lblNewLabel, "cell 0 3,alignx left");

		JLabel lblHomoSapiens = new JLabel("Homo Sapiens (9606)");
		panel.add(lblHomoSapiens, "cell 1 3");

		JLabel lblGeneName = new JLabel("Gene name:");
		panel.add(lblGeneName, "flowx,cell 0 4,alignx left");

		JLabel lblCat = new JLabel("CAT");
		panel.add(lblCat, "cell 1 4");

		JLabel lblSynonyms = new JLabel("Synonyms:");
		panel.add(lblSynonyms, "flowx,cell 0 5,alignx left,aligny top");

		final JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1, "cell 1 5,grow");

		final JList<String> list = new JList<String>();
		list.setModel(new AbstractListModel() {
			String[] values = new String[] { "toto", "itit" };

			public int getSize() {
				return values.length;
			}

			public Object getElementAt(int index) {
				return values[index];
			}
		});
		scrollPane_1.setViewportView(list);

		scrollPane_1.setVisible(false);

		final JToggleButton toggleButton = new JToggleButton("+");
		toggleButton.setMargin(new Insets(2, 5, 2, 5));

		panel.add(toggleButton, "cell 2 5,alignx center,aligny top");

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Orthologs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, "cell 0 6 3 1,grow");

		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
				if (state == ItemEvent.SELECTED) {
					toggleButton.setText("-");
					scrollPane_1.setVisible(true);

				} else {
					toggleButton.setText("+");
					scrollPane_1.setVisible(false);
				}
				panel.validate();
			}
		};
		toggleButton.addItemListener(itemListener);

		JLabel lblP = new JLabel("P04040");
		panel.add(lblP, "cell 1 1");

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBorder(null);
		panel.add(scrollPane_2, "cell 0 7 3 1,grow");

		final JPanel panel_2 = new JPanel();
		scrollPane_2.setViewportView(panel_2);
		panel_2.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Gene Ontology", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setLayout(new BorderLayout(0, 0));

		JTree tree = new JTree();
		tree.setRootVisible(false);
		tree.setOpaque(false);
		panel_2.add(tree);
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	// End of variables declaration//GEN-END:variables

	public static void main(String[] argv) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}
		JFrame j = new JFrame();
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ResultPanel r = new ResultPanel();
		j.getContentPane().add(r);
		j.pack();
		j.setVisible(true);
	}
}

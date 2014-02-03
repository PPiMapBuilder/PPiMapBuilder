package tk.nomis_tech.ppimapbuilder.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
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

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import net.miginfocom.swing.MigLayout;

public class ResultPanel extends javax.swing.JPanel implements CytoPanelComponent {

	private final String title = "PPiMapBuilder";

	/**
	 * Creates new form ResultPanel
	 */

	private ImageIcon logo = new ImageIcon(getClass().getResource("logo.png"));

	private ImageIcon urlIcon = new ImageIcon(getClass().getResource("external_link_icon.gif"));
	private final JLabel lblReviewed = new JLabel(new ImageIcon(getClass().getResource("star.png")));
	private final JLabel lblUnreviewed = new JLabel(new ImageIcon(getClass().getResource("unstar.png")));

	private final JPanel mainPanel;

	private boolean isReviewed = true;
	private JLabel lblPtnname;
	private JLabel proteinId;
	private JLabel ecNum;
	private JLabel organism;
	private JLabel geneName;
	private JTree treeOntology;
	private JList<String> geneNameSynonyms;

	public ResultPanel() {
		setPreferredSize(new Dimension(320, 400));
		setBorder(null);
		setLayout(new BorderLayout(0, 0));

		final JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		mainPanel = new JPanel();
		mainPanel.setBorder(null);
		scrollPane.setViewportView(mainPanel);
		mainPanel.setLayout(new MigLayout("hidemode 3", "[70px:70px:70px,grow,right][grow][]", "[][][][][][::50px][10px:n][30px:80px,grow]"));

//		 setDefaultView();
		setTestProteinView();

		initComponents();
	}

	private void setDefaultView() {
		JLabel lblEmpty = new JLabel("Please select one node/protein.");
		lblEmpty.setForeground(Color.DARK_GRAY);
		mainPanel.add(lblEmpty, "cell 0 0 3 7,alignx center,aligny center");
	}

	// TODO: private void setProteinView(Protein ptn) {
	private void setTestProteinView() {
		/*
		 * HEADER - GENERAL INFORMATION
		 */
		lblPtnname = new JLabel("Catalase");
		lblPtnname.setBorder(new EmptyBorder(3, 8, 3, 3));
		lblPtnname.setFont(new Font("Tahoma", Font.PLAIN, 22));
		mainPanel.add(lblPtnname, "cell 0 0 2 1,grow");

		final JLabel lblUniprotId = new JLabel("Uniprot ID:");
		mainPanel.add(lblUniprotId, "cell 0 1,alignx left");

		final JLabel lblEcNumber = new JLabel("EC Number:");
		mainPanel.add(lblEcNumber, "flowx,cell 0 2,alignx left");

		final JLabel lblNewLabel = new JLabel("Organism:");
		mainPanel.add(lblNewLabel, "cell 0 3,alignx left");

		final JLabel lblGeneName = new JLabel("Gene name:");
		mainPanel.add(lblGeneName, "flowx,cell 0 4,alignx left");

		final JLabel lblSynonyms = new JLabel("Synonyms:");
		mainPanel.add(lblSynonyms, "flowx,cell 0 5,alignx left,aligny top");

		final JScrollPane scrollPane_1 = new JScrollPane();
		mainPanel.add(scrollPane_1, "cell 1 5,grow");

		final JToggleButton toggleButton = new JToggleButton("+");
		toggleButton.setMargin(new Insets(2, 5, 2, 5));
		mainPanel.add(toggleButton, "cell 2 5,alignx center,aligny top");

		JScrollPane scrollPane_Orthologs = new JScrollPane();
		scrollPane_Orthologs.setBorder(null);
		mainPanel.add(scrollPane_Orthologs, "cell 0 6 3 1,grow");

		final JPanel panel_Orthologs = new JPanel();
		scrollPane_Orthologs.setViewportView(panel_Orthologs);
		panel_Orthologs
				.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Orthologs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_Orthologs.setLayout(new BoxLayout(panel_Orthologs, BoxLayout.Y_AXIS));

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
				mainPanel.validate();
			}
		};
		toggleButton.addItemListener(itemListener);

		final JScrollPane scrollPane_GO = new JScrollPane();
		scrollPane_GO.setBorder(null);
		mainPanel.add(scrollPane_GO, "cell 0 7 3 1,grow");

		final JPanel panel_GO = new JPanel();
		scrollPane_GO.setViewportView(panel_GO);
		panel_GO.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Gene Ontology", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_GO.setLayout(new BorderLayout(0, 0));

		/*
		 * VARIABLE LABELS
		 */
		proteinId = new JLabel("P04040");
		mainPanel.add(proteinId, "cell 1 1");

		ecNum = new JLabel("1.11.1.6");
		mainPanel.add(ecNum, "cell 1 2");

		organism = new JLabel("Homo Sapiens (9606)");
		mainPanel.add(organism, "cell 1 3");

		geneName = new JLabel("CAT");
		mainPanel.add(geneName, "cell 1 4");

		geneNameSynonyms = new JList<String>();
		geneNameSynonyms.setModel(new DefaultListModel<String>() {
			{
				addElement("ototo");
				addElement("hatikado");
			}
		});

		scrollPane_1.setViewportView(geneNameSynonyms);
		scrollPane_1.setVisible(false);

		treeOntology = new JTree();
		treeOntology.setRootVisible(false);
		treeOntology.setOpaque(false);
		panel_GO.add(treeOntology);

		/*
		 * ICONS
		 */
		{
			if (isReviewed)
				mainPanel.add(lblReviewed, "cell 2 0,alignx right,aligny center");
			else
				mainPanel.add(lblReviewed, "cell 2 0,alignx right,aligny center");

			JLabel lblExtLinkUniprot = new JLabel(urlIcon);
			lblExtLinkUniprot.setToolTipText("Access external link");
			mainPanel.add(lblExtLinkUniprot, "cell 2 1,alignx right,aligny center");

			JLabel lblExtLinkECnum = new JLabel(urlIcon);
			lblExtLinkECnum.setToolTipText("Access external link");
			mainPanel.add(lblExtLinkECnum, "cell 2 2,alignx right,aligny center");

			JLabel lblExtLinkOrganism = new JLabel(urlIcon);
			lblExtLinkOrganism.setToolTipText("Access external link");
			mainPanel.add(lblExtLinkOrganism, "cell 2 3,alignx right,aligny center");

			JLabel lblExtLinkGenename = new JLabel(urlIcon);
			lblExtLinkGenename.setToolTipText("Access external link");
			mainPanel.add(lblExtLinkGenename, "cell 2 4,alignx right,aligny center");
		}
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

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
}

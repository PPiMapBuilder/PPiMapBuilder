package tk.nomis_tech.ppimapbuilder.ui.resultpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.NotImplementedException;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.OpenBrowser;

import tk.nomis_tech.ppimapbuilder.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.ui.util.JHyperlinkLabel;

import com.eclipsesource.json.JsonObject;

/**
 * Creates new ResultPanel form
 */
@SuppressWarnings("serial")
public class ResultPanel extends javax.swing.JPanel implements CytoPanelComponent {

	/**
	 * Tab title in Cytoscape Result panel
	 */
	private final String title = "PPiMapBuilder summary";

	private static final ImageIcon PMB_LOGO = new ImageIcon(ResultPanel.class.getResource("/pmblogo.png"));

	/**
	 * Cytoscape service to open link into web browser
	 */
	private final OpenBrowser openBrowser;

	/**
	 * Icon for external links
	 */
	private static final ImageIcon ICN_EXTERNAL_LINK = new ImageIcon(ResultPanel.class.getResource("external_link_icon.gif"));
	/**
	 * Icon for reviewed protein
	 */
	private static final ImageIcon ICN_REVIEWED = new ImageIcon(ResultPanel.class.getResource("star.png"));
	/**
	 * Icon for unreviewed protein
	 */
	private static final ImageIcon ICN_UNREVIEWED = new ImageIcon(ResultPanel.class.getResource("unstar.png"));

	/**
	 * Default font if nothing to show (italic)
	 */
	private static final Font NONE_FONT = new Font(null, Font.ITALIC, 11);
	/**
	 * Standard font for text
	 */
	private static final Font STD_FONT = new Font(null, Font.PLAIN, 11);

	/**
	 * Custom Gene Ontology tree appearance
	 */
	private static final TreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {
		{
			ImageIcon ICN_OPEN = new ImageIcon(ResultPanel.class.getResource("open.gif"));
			ImageIcon ICN_CLOSED = new ImageIcon(ResultPanel.class.getResource("close.gif"));
			ImageIcon ICN_LEAF = new ImageIcon(ResultPanel.class.getResource("leaf.png"));
			setOpaque(true);
			setOpenIcon(ICN_OPEN);
			setClosedIcon(ICN_CLOSED);
			setLeafIcon(ICN_LEAF);
			// setBackground(new
			// Color(UIManager.getColor("Button.background").getRed(),
			// UIManager.getColor("Button.background").getGreen(),
			// UIManager.getColor("Button.background").getBlue()));
			// setForeground(new
			// Color(UIManager.getColor("Panel.foreground").getRed(),
			// UIManager.getColor("Panel.foreground").getGreen(),
			// UIManager.getColor("Panel.foreground").getBlue()));
			setTextSelectionColor(getTextNonSelectionColor());
		}
	};

	/**
	 * Main panel, containing all the others
	 */
	private JPanel mainPanel = new JPanel();
	/**
	 * Default view (if no protein selected)
	 */
	private JPanel voidPanel = new JPanel();
	/**
	 * Protein view
	 */
	private JPanel proteinPanel = new JPanel();
	private JPanel panelOrthologs;
	private JTree treeOrthologs;

	private JLabel ptnName;
	private JLabel lblReviewed;
	private JLabel proteinId;
	private JLabel ecNum;
	private JLabel organism;
	private JLabel geneName;
	private DefaultListModel synonymsList;
	private JTree treeOntology;
	private DefaultMutableTreeNode treeModelGO;

	private JHyperlinkLabel lblExtLinkUniprot;
	private JHyperlinkLabel lblExtLinkECnum;
	private JHyperlinkLabel lblExtLinkOrganism;
	private JHyperlinkLabel lblExtLinkGenename;

	/**
	 * Interaction view
	 */
	private JPanel interactionPanel = new JPanel();
	private JLabel intAName;
	private JLabel intBName;
	private JLabel taxId;
	private JPanel panelPubId;
	private JTree treePubId;
	private JPanel panelSource;
	private JTree treeSource;
	private JPanel panelConfidence;
	private JTree treeConfidence;
	
	
	/**
	 * Build a panel containing all the needed elements.
	 * 
	 * @param openBrowser
	 */
	public ResultPanel(OpenBrowser openBrowser) {
		this.openBrowser = openBrowser;

		setPreferredSize(new Dimension(260, 400));
		setBorder(null);
		setLayout(new BorderLayout(0, 0));

		final JScrollPane scrollPane_main = new JScrollPane();
		scrollPane_main.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_main.setBorder(null);
		add(scrollPane_main, BorderLayout.CENTER);

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(null);
		scrollPane_main.setViewportView(mainPanel);

		/* Build default panel */
		this.setDefaultView();
		this.setVisible(true);

		/* Build protein view panel */
		this.setStaticProteinView();
		proteinPanel.setVisible(false);

		/* Build interaction view panel */
		this.setStaticInteractionView();
		interactionPanel.setVisible(false);		
		
		mainPanel.add(voidPanel);
		mainPanel.add(proteinPanel);
		mainPanel.add(interactionPanel);
	}

	/**
	 * Build the default view. When no or 2+ nodes are clicked.
	 */
	private void setDefaultView() {
		JLabel lblEmpty = new JLabel("Please select one node/protein.");
		lblEmpty.setForeground(Color.DARK_GRAY);
		voidPanel.setLayout(new MigLayout("hidemode 3", "[grow,center]", "[80px:n,center][]"));
		voidPanel.add(lblEmpty, "cell 0 0");

		JLabel lblLogo = new JLabel(PMB_LOGO);
		voidPanel.add(lblLogo, "cell 0 1");

		voidPanel.setVisible(true);
	}

	/**
	 * Build the protein summary view. Display titles and default value (they
	 * may not be needed).
	 */
	private void setStaticProteinView() {

		proteinPanel.setLayout(new MigLayout("hidemode 3", "[70px:70px:70px,grow,right]10[grow][]", "[][][][][][::50px][10px:n][30px:80px,grow]"));
		/*
		 * HEADER - GENERAL INFORMATION
		 */
		ptnName = new JLabel("Protein View");
		ptnName.setBorder(new EmptyBorder(3, 8, 3, 0));
		ptnName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		proteinPanel.add(ptnName, "cell 0 0 2 1,grow");

		final JLabel lblUniprotId = new JLabel("Uniprot ID:");
		proteinPanel.add(lblUniprotId, "cell 0 1,alignx left");

		final JLabel lblEcNumber = new JLabel("EC Number:");
		proteinPanel.add(lblEcNumber, "flowx,cell 0 2,alignx left");

		final JLabel lblNewLabel = new JLabel("Organism:");
		proteinPanel.add(lblNewLabel, "cell 0 3,alignx left");

		final JLabel lblGeneName = new JLabel("Gene name:");
		proteinPanel.add(lblGeneName, "flowx,cell 0 4,alignx left");

		final JLabel lblSynonyms = new JLabel("Synonyms:");
		proteinPanel.add(lblSynonyms, "flowx,cell 0 5,alignx left,aligny top");

		final JScrollPane scrollPane_Synonyms = new JScrollPane();
		proteinPanel.add(scrollPane_Synonyms, "cell 1 5,grow");

		final JToggleButton toggleButton = new JToggleButton("+");
		toggleButton.setMargin(new Insets(2, 5, 2, 5));
		toggleButton.setBorder(new LineBorder(Color.black, 1));
		toggleButton.setBackground(Color.GRAY);
		proteinPanel.add(toggleButton, "cell 2 5,alignx center,aligny top");

		final JScrollPane scrollPane_Orthologs = new JScrollPane();
		scrollPane_Orthologs.setOpaque(false);
		scrollPane_Orthologs.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Orthologs", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		proteinPanel.add(scrollPane_Orthologs, "cell 0 6 3 1,grow");

		panelOrthologs = new JPanel();
		scrollPane_Orthologs.setViewportView(panelOrthologs);
		panelOrthologs.setBorder(null);
		panelOrthologs.setLayout(new BoxLayout(panelOrthologs, BoxLayout.Y_AXIS));

		treeOrthologs = new JTree();
		panelOrthologs.add(treeOrthologs);

		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
				if (state == ItemEvent.SELECTED) {
					toggleButton.setText("-");
					scrollPane_Synonyms.setVisible(true);
				} else {
					toggleButton.setText("+");
					scrollPane_Synonyms.setVisible(false);
				}
			}
		};
		toggleButton.addItemListener(itemListener);

		final JScrollPane scrollPane_GO = new JScrollPane();
		scrollPane_GO.setOpaque(false);
		/*
		 * scrollPane_GO.setVerticalScrollBarPolicy(ScrollPaneConstants.
		 * VERTICAL_SCROLLBAR_NEVER); scrollPane_GO
		 * .setHorizontalScrollBarPolicy
		 * (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		 */
		scrollPane_GO.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Gene Ontology", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		proteinPanel.add(scrollPane_GO, "cell 0 7 3 1,grow");

		final JPanel panel_GO = new JPanel();
		scrollPane_GO.setViewportView(panel_GO);
		panel_GO.setBorder(null);
		panel_GO.setLayout(new BorderLayout(0, 0));

		/*
		 * VARIABLE LABELS
		 */
		proteinId = NONE_LABEL();
		proteinPanel.add(proteinId, "cell 1 1");

		ecNum = NONE_LABEL();
		proteinPanel.add(ecNum, "cell 1 2");

		organism = NONE_LABEL();
		proteinPanel.add(organism, "cell 1 3");

		geneName = NONE_LABEL();
		proteinPanel.add(geneName, "cell 1 4");

		JList geneNameSynonyms = new JList();
		geneNameSynonyms.setVisibleRowCount(3);
		geneNameSynonyms.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		synonymsList = new DefaultListModel();
		synonymsList.addElement("");
		geneNameSynonyms.setModel(synonymsList);

		scrollPane_Synonyms.setViewportView(geneNameSynonyms);
		scrollPane_Synonyms.setVisible(false);

		treeModelGO = new DefaultMutableTreeNode("GeneOntology");
		treeOntology = new JTree(treeModelGO);
		treeOntology.setRootVisible(false);
		treeOntology.setOpaque(false);
		treeOntology.setCellRenderer(cellRenderer);
		treeOntology.setToggleClickCount(1);
		panel_GO.add(treeOntology);
		/*
		 * ICONS
		 */
		{
			lblReviewed = new JLabel();

			// www.uniprot.org/uniprot/ link
			this.lblExtLinkUniprot = new JHyperlinkLabel(openBrowser);
			this.lblExtLinkUniprot.setVisible(false);
			this.lblExtLinkUniprot.setIcon(ICN_EXTERNAL_LINK);
			this.lblExtLinkUniprot.setToolTipText("View on Uniprot Entry");
			this.lblExtLinkUniprot.makeClickable();

			// enzyme.expasy.org/EC/ link
			this.lblExtLinkECnum = new JHyperlinkLabel(openBrowser);
			this.lblExtLinkECnum.setVisible(false);
			this.lblExtLinkECnum.setIcon(ICN_EXTERNAL_LINK);
			this.lblExtLinkECnum.setToolTipText("View on ExPASy");
			this.lblExtLinkECnum.makeClickable();

			// www.uniprot.org/taxonomy/ link
			this.lblExtLinkOrganism = new JHyperlinkLabel(openBrowser);
			this.lblExtLinkOrganism.setVisible(false);
			this.lblExtLinkOrganism.setIcon(ICN_EXTERNAL_LINK);
			this.lblExtLinkOrganism.setToolTipText("View on Uniprot Taxonomy");
			this.lblExtLinkOrganism.makeClickable();

			this.lblExtLinkGenename = new JHyperlinkLabel(openBrowser);
			this.lblExtLinkGenename.setVisible(false);
			this.lblExtLinkGenename.setIcon(ICN_EXTERNAL_LINK);
			this.lblExtLinkGenename.setToolTipText("View on NCBI Gene");
			this.lblExtLinkGenename.makeClickable();

			proteinPanel.add(lblReviewed, "cell 2 0,alignx right,aligny center");
			proteinPanel.add(lblExtLinkUniprot, "cell 2 1,alignx right,aligny center");
			proteinPanel.add(lblExtLinkECnum, "cell 2 2,alignx right,aligny center");
			proteinPanel.add(lblExtLinkOrganism, "cell 2 3,alignx right,aligny center");
			proteinPanel.add(lblExtLinkGenename, "cell 2 4,alignx right,aligny center");
		}

	}

	/**
	 * Add CyRow data to the protein panel.
	 * 
	 * @param row
	 */
	public void setProteinView(CyRow row) {

		this.setProteinId(row.get("uniprot_id", String.class));
		this.setPtnName(row.get("protein_name", String.class));
		this.setReviewState(row.get("reviewed", String.class).equalsIgnoreCase("true"));
		this.setGeneName(row.get("gene_name", String.class), row.get("tax_id", String.class));

		this.setEcNumber(row.get("ec_number", String.class) != null ? row.get("ec_number", String.class) : "");
		//
		this.setGeneNameSynonyms(row.getList("synonym_gene_names", String.class));
		this.setOntology(row.getList("biological_processes_hidden", String.class), row.getList("cellular_components_hidden", String.class),
				row.getList("molecular_functions_hidden", String.class));
		this.setOrganism(row.get("tax_id", String.class));
		this.setOrhologs(row.getList("orthologs", String.class));
		
		this.showProteinView();
	}
	
	private void setStaticInteractionView() {

		interactionPanel.setLayout(new MigLayout("hidemode 3", "[70px:70px:70px,grow,right]10[grow][]", "[][][][][][::50px][10px:n][30px:80px,grow]"));
		/*
		 * HEADER - GENERAL INFORMATION
		 */
		intAName = new JLabel("Interaction View");
		intAName.setBorder(new EmptyBorder(3, 8, 3, 0));
		intAName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		interactionPanel.add(intAName, "cell 0 0 2 1,grow");
		
		intBName = new JLabel();
		intBName.setBorder(new EmptyBorder(3, 8, 3, 0));
		intBName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		interactionPanel.add(intBName, "cell 0 1 2 1,grow");
		
		final JLabel lblTaxId = new JLabel("Tax ID:");
		interactionPanel.add(lblTaxId, "cell 0 2,alignx left");
		
		final JScrollPane scrollPane_Publication = new JScrollPane();
		scrollPane_Publication.setOpaque(false);
		scrollPane_Publication.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Publication", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		interactionPanel.add(scrollPane_Publication, "cell 0 3 3 1,grow");
		
		panelPubId = new JPanel();
		scrollPane_Publication.setViewportView(panelPubId);
		panelPubId.setBorder(null);
		panelPubId.setLayout(new BoxLayout(panelPubId, BoxLayout.Y_AXIS));

		//treePubId = new JTree();
		//panelPubId.add(treePubId);
		
		final JScrollPane scrollPane_Source = new JScrollPane();
		scrollPane_Source.setOpaque(false);
		scrollPane_Source.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Source", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		interactionPanel.add(scrollPane_Source, "cell 0 4 3 1,grow");
		
		panelSource = new JPanel();
		scrollPane_Source.setViewportView(panelSource);
		panelSource.setBorder(null);
		panelSource.setLayout(new BoxLayout(panelSource, BoxLayout.Y_AXIS));

		treeSource = new JTree();
		panelSource.add(treeSource);
		
		final JScrollPane scrollPane_Confidence = new JScrollPane();
		scrollPane_Confidence.setOpaque(false);
		scrollPane_Confidence.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Confidence", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		interactionPanel.add(scrollPane_Confidence, "cell 0 5 3 1,grow");
		
		panelConfidence = new JPanel();
		scrollPane_Confidence.setViewportView(panelConfidence);
		panelConfidence.setBorder(null);
		panelConfidence.setLayout(new BoxLayout(panelConfidence, BoxLayout.Y_AXIS));

		treeConfidence = new JTree();
		panelConfidence.add(treeConfidence);
		
		
		/*
		 * VARIABLE LABELS
		 */
		taxId = NONE_LABEL();
		interactionPanel.add(taxId, "cell 1 2");


	}

	public void setInteractionView(CyRow row) {
		//this.setIntAName(row.get("protein_name", String.class));
		this.setIntAName(row.get("Protein_name_A", String.class));
		this.setIntBName(row.get("Protein_name_B", String.class));
		this.setTaxId(row.get("tax_id", String.class));
		this.setPubId(row.getList("pubid", String.class));
		this.setSource(row.getList("source", String.class));
		this.setConfidence(row.getList("confidence", String.class));

		this.showInteractionView();

	}
	
	public void showInteractionView() {
		voidPanel.setVisible(false);

		proteinPanel.setVisible(false);

		interactionPanel.setVisible(true);

		this.repaint();
	}
	
	/**
	 * Display the default view. When no node or 2+ nodes are selected.
	 */
	public void showDefaultView() {
		proteinPanel.setVisible(false);
		interactionPanel.setVisible(false);
		voidPanel.setVisible(true);

		this.repaint();
	}

	/**
	 * Display the protein summary view. When 1 single protein is selected.
	 */
	public void showProteinView() {
		voidPanel.setVisible(false);
		interactionPanel.setVisible(false);
		proteinPanel.setVisible(true);

		this.repaint();
	}

	/**
	 * Get protein name as displayed in the label.
	 * 
	 * @return protein name
	 */
	public String getPtnName() {
		return ptnName.getText();
	}

	/**
	 * Set the protein name label
	 * 
	 * @param protein
	 *            name
	 */
	public void setPtnName(String ptnName) {
		if (!ptnName.isEmpty()) {
			if (ptnName.length() > 20) {
				this.ptnName.setText(ptnName.substring(0, 17) + "...");
			} else {
				this.ptnName.setText(ptnName);
			}
			this.ptnName.setToolTipText(ptnName);

		} else {
			this.ptnName.setFont(NONE_FONT);
			this.ptnName.setText("none");
		}
	}

	/**
	 * Set the Tax ID
	 * 
	 * @param taxId
	 */
	private void setTaxId(String taxId) {
		if (!taxId.isEmpty()) {
			this.taxId.setFont(STD_FONT);
			this.taxId.setText(taxId);
			this.lblExtLinkUniprot.setVisible(true);
		} else {
			this.lblExtLinkUniprot.setVisible(false);
			this.taxId.setFont(NONE_FONT);
			this.taxId.setText("none");
		}
	}
	
	/**
	 * Set pubId
	 * 
	 * @param pubid
	 */
	private void setPubId(List<String> pubId) {
		this.panelPubId.removeAll();
		
		System.out.println(pubId);

		for (String str : pubId) {

			JPanel publication = new JPanel();
			publication.setLayout(new BoxLayout(publication, BoxLayout.LINE_AXIS));
			
			publication.add(new JLabel(" •   " + str));
			publication.add(Box.createRigidArea(new Dimension(5,0)));
			JHyperlinkLabel lblExtLinkPubMed;
			lblExtLinkPubMed = new JHyperlinkLabel(openBrowser);
			lblExtLinkPubMed.setVisible(false);
			lblExtLinkPubMed.setIcon(ICN_EXTERNAL_LINK);
			lblExtLinkPubMed.makeClickable();
			publication.add(lblExtLinkPubMed);
			
			String[] parts = str.split(":");
			if (parts[0].equals("pubmed")){
				System.out.println("pubmed");
				
				try {
					lblExtLinkPubMed.setVisible(true);

					lblExtLinkPubMed.setToolTipText("View on PubMed Publication");
					lblExtLinkPubMed.setUri(new URI("http://www.ncbi.nlm.nih.gov/pubmed/?term=" + parts[1]));
					
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			this.panelPubId.add(publication);
			publication.setAlignmentX(LEFT_ALIGNMENT);
		}
	}
	
	/**
	 * Set source
	 * 
	 * @param source
	 */
	private void setSource(List<String> source) {
		this.panelSource.removeAll();
		for (String str : source) {
			this.panelSource.add(new JLabel(" •   " + str));
		}
	}
	
	/**
	 * Set source
	 * 
	 * @param source
	 */
	private void setConfidence(List<String> confidence) {
		this.panelConfidence.removeAll();
		for (String str : confidence) {
			this.panelConfidence.add(new JLabel(" •   " + str));
		}
	}
	
	/**
	 * Get protein name as displayed in the label.
	 * 
	 * @return gene name
	 */
	public String getGeneName() {
		return geneName.getText();
	}

	/**
	 * Set the protein name label
	 * 
	 * @param gene
	 *            name
	 * @param taxonomic
	 *            id
	 */
	private void setGeneName(String geneName, String taxId) {
		if (!geneName.isEmpty()) {
			this.geneName.setFont(STD_FONT);
			this.geneName.setText(geneName);
			// http://www.ncbi.nlm.nih.gov/gene?term=(proS[Gene Name]) AND
			// 9606[Taxonomy ID])
			try {
				this.lblExtLinkGenename.setUri(new URI("http://www.ncbi.nlm.nih.gov/gene?term=(" + geneName + "%5BGene%20Name%5D)%20AND%20" + taxId
						+ "%5BTaxonomy%20ID%5D"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkGenename.setVisible(true);

		} else {
			this.lblExtLinkGenename.setVisible(false);
			this.geneName.setFont(NONE_FONT);
			this.geneName.setText("none");
		}
	}

	/**
	 * Get the protein reviewed status
	 * 
	 * @return
	 */
	public boolean getReviewState() {
		return lblReviewed.getIcon().equals(ICN_REVIEWED);
	}

	/**
	 * Set the protein review status
	 * 
	 * @param reviewed
	 */
	private void setReviewState(boolean reviewed) {
		if (reviewed) {
			lblReviewed.setIcon(ICN_REVIEWED);
			lblReviewed.setToolTipText("Reviewed protein");
		} else {
			lblReviewed.setIcon(ICN_UNREVIEWED);
			lblReviewed.setToolTipText("Unreviewed protein");
		}

	}

	/**
	 * Get protein name as displayed in the label.
	 * 
	 * @return
	 */
	public String getProteinId() {
		return proteinId.getText();
	}

	/**
	 * Set the Uniprot ID
	 * 
	 * @param uniprotId
	 */
	private void setProteinId(String uniprotId) {
		if (!uniprotId.isEmpty()) {
			this.proteinId.setFont(STD_FONT);
			this.proteinId.setText(uniprotId);
			try {
				this.lblExtLinkUniprot.setUri(new URI("http://www.uniprot.org/uniprot/" + uniprotId));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkUniprot.setVisible(true);
		} else {
			this.lblExtLinkUniprot.setVisible(false);
			this.proteinId.setFont(NONE_FONT);
			this.proteinId.setText("none");
		}
	}

	/**
	 * Get the Enzyme Classification number as displayed in the label.
	 * 
	 * @return
	 */
	public String getEcNumber() {
		return ecNum.getText();
	}

	/**
	 * Set the Enzyme Classification number
	 * 
	 * @param ecNumber
	 */
	private void setEcNumber(String ecNumber) {
		if (!ecNumber.isEmpty()) {
			this.ecNum.setFont(STD_FONT);
			this.ecNum.setText(ecNumber);
			try {
				this.lblExtLinkECnum.setUri(new URI("http://enzyme.expasy.org/EC/" + ecNumber));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkECnum.setVisible(true);

		} else {
			this.lblExtLinkECnum.setVisible(false);
			this.ecNum.setFont(NONE_FONT);
			this.ecNum.setText("none");
		}
	}

	/**
	 * Get the organism as displayed in the label.
	 * 
	 * @return
	 */
	public String getOrganism() {
		return organism.getText();
	}

	/**
	 * Set the organism taxonomic ID
	 * 
	 * @param taxId
	 */
	@Deprecated
	private void setOrganism(String taxId) {
		setOrganism(null, taxId);
	}

	/**
	 * Set the organism name and taxonomic ID
	 * 
	 * @param taxId
	 */
	private void setOrganism(String name, String taxId) {
		String s;
		if (name == null || name.isEmpty()) {
			s = "[" + taxId + "]";
		} else {
			s = name + " [" + taxId + "]";
		}

		if (!s.isEmpty()) {
			this.organism.setFont(STD_FONT);
			this.organism.setText(s);
			try {
				this.lblExtLinkOrganism.setUri(new URI("http://www.uniprot.org/taxonomy/" + taxId));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkOrganism.setVisible(true);
		} else {
			this.lblExtLinkOrganism.setVisible(false);
			this.organism.setFont(NONE_FONT);
			this.organism.setText("none");
		}

	}

	/**
	 * Get the gene names synonyms
	 * 
	 * @return
	 */
	public List<String> getGeneNameSynonyms() {
		return new ArrayList<String>(synonymsList.size()) {
			{
				for (int i = 0; i < synonymsList.size(); i++) {
					add((String) synonymsList.get(i));
				}
			}
		};
	}

	/**
	 * Set the gene name synonyms
	 * 
	 * @param synonyms
	 */
	private void setGeneNameSynonyms(List<String> synonyms) {
		synonymsList.clear();
		if (!synonyms.isEmpty()) {
			for (String s : synonyms) {
				synonymsList.addElement(s);
			}
		} else {
			synonymsList.addElement("");
		}
	}

	/**
	 * Set orthologs
	 * 
	 * @param orthologs
	 */
	private void setOrhologs(List<String> orthologs) {
		// TODO: Add orthologs column
		// throw new NotImplementedException("Ortholog are not handle yet");
		this.panelOrthologs.removeAll();
		for (String str : orthologs) {
			JsonObject json = JsonObject.readFrom(str);
			this.panelOrthologs.add(new JLabel(" •   " + json.get("uniProtId").asString() + " [" + json.get("taxId").asInt() + "]"));
		}

	}

	/**
	 * Get orthologs
	 * 
	 * @return
	 */
	public List<OrthologProtein> getOrthologs() {
		// TODO: Add orthologs column
		throw new NotImplementedException("Ortholog are not handle yet");
	}

	/**
	 * Set the Gene Ontology
	 * 
	 * @param biologicalProcess
	 * @param cellularComponent
	 * @param molecularFunction
	 */
	private void setOntology(final List<String> biologicalProcess, final List<String> cellularComponent, final List<String> molecularFunction) {
		treeModelGO.removeAllChildren();
		try {
			treeModelGO.add(new DefaultMutableTreeNode("Biological process (" + biologicalProcess.size() + ")") {
				{
					if (biologicalProcess != null && !biologicalProcess.isEmpty())
						for (String s : biologicalProcess) {
							JsonObject obj = JsonObject.readFrom(s);
							String process = new StringBuilder(obj.get("term").asString()).append(" [").append(obj.get("id").asString()).append("]").toString();
							add(new DefaultMutableTreeNode(process));
						}
				}
			});

			treeModelGO.add(new DefaultMutableTreeNode("Cellular componant (" + cellularComponent.size() + ")") {
				{
					if (cellularComponent != null && !cellularComponent.isEmpty())
						for (String s : cellularComponent) {
							JsonObject obj = JsonObject.readFrom(s);
							String component = new StringBuilder(obj.get("term").asString()).append(" [").append(obj.get("id").asString()).append("]").toString();
							add(new DefaultMutableTreeNode(component));
						}
				}
			});

			treeModelGO.add(new DefaultMutableTreeNode("Molecular function (" + molecularFunction.size() + ")") {
				{
					if (molecularFunction != null && !molecularFunction.isEmpty())
						for (String s : molecularFunction) {
							JsonObject obj = JsonObject.readFrom(s);
							String function = new StringBuilder(obj.get("term").asString()).append(" [").append(obj.get("id").asString()).append("]").toString();
							add(new DefaultMutableTreeNode(function));
						}
				}
			});
			treeOntology.setModel(new DefaultTreeModel(treeModelGO));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> getOntology() {
		throw new NotImplementedException("Ortholog are not handle yet");
	}

	/**
	 * Build a default JLabel ("none" in italic)
	 * 
	 * @return JLabel
	 */
	private JLabel NONE_LABEL() {
		return new JLabel("none") {
			{
				setFont(NONE_FONT);
			}
		};
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

	/**
	 * Get interactor name as displayed in the label.
	 * 
	 * @return protein name
	 */
	public String getIntAName() {
		return intAName.getText();
	}

	/**
	 * Set the protein name label
	 * 
	 * @param protein
	 *            name
	 */
	public void setIntAName(String intAName) {
		if (!intAName.isEmpty()) {
			if (intAName.length() > 20) {
				this.intAName.setText(intAName.substring(0, 17) + "...");
			} else {
				this.intAName.setText(intAName);
			}
			this.intAName.setToolTipText(intAName);

		} else {
			this.intAName.setFont(NONE_FONT);
			this.intAName.setText("none");
		}
	}
	
	/**
	 * Get interactor name as displayed in the label.
	 * 
	 * @return protein name
	 */
	public String getIntBName() {
		return intBName.getText();
	}

	/**
	 * Set the protein name label
	 * 
	 * @param protein
	 *            name
	 */
	public void setIntBName(String intBName) {
		if (!intBName.isEmpty()) {
			if (intBName.length() > 20) {
				this.intBName.setText(intBName.substring(0, 17) + "...");
			} else {
				this.intBName.setText(intBName);
			}
			this.intBName.setToolTipText(intBName);

		} else {
			this.intBName.setFont(NONE_FONT);
			this.intBName.setText("none");
		}
	}
	
	

}

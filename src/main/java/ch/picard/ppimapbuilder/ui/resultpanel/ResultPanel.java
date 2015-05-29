/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.ui.resultpanel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.ui.util.label.JHyperlinkLabel;
import com.eclipsesource.json.JsonObject;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.NotImplementedException;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.OpenBrowser;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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

	/*
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
			setTextSelectionColor(getTextNonSelectionColor());
		}
	};

	/**
	 * Main panel, containing all the others
	 */
	private JPanel mainPanel = new JPanel();
	private CyRow myRow = null;
	/**
	 * Default view (if no protein selected)
	 */
	private JPanel voidPanel = new JPanel();
	/**
	 * Protein view
	 */
	private JPanel proteinPanel = new JPanel();
	
	private JLabel ptnName;
	private JLabel lblReviewed;
	private JLabel proteinId;
	private JLabel ecNum;
	private JLabel proteinOrganism;
	private JLabel geneName;

	private JLabel lblCluster;
	private JLabel goCluster;
	
	private JScrollPane scrollPane_Synonyms;
	private JPanel panelSynonyms;
	private JTree treeSynonyms;
	private DefaultListModel synonymsList;

	private JScrollPane scrollPane_Orthologs;
	private JPanel panelOrthologs;
	private JTree treeOrthologs;

	private JScrollPane scrollPane_GO;
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
	private String intANameText;
	private String intBNameText;
	private JLabel interactionOrganism;
	private String intOrgaText;

	private JScrollPane scrollPane_Source;
	private JPanel panelSource;
	private JTree treeSource;

	private JScrollPane scrollPane_Type;
	private JPanel panelType;
	private JTree treeType;

	private JScrollPane scrollPane_DetMeth;
	private JPanel panelDetMeth;
	private JTree treeDetMeth;

	private JScrollPane scrollPane_Confidence;
	private JPanel panelConfidence;
	private JTree treeConfidence;

	/**
	 * Background task monitor
	 */
	private BackgroundTaskMonitor backgroundTaskPanel = null;

	public void setBackgroundTask(BackgroundTaskMonitor backgroundTaskMonitor) {
		if(backgroundTaskMonitor == null && this.backgroundTaskPanel != null) {
			remove(this.backgroundTaskPanel);
			repaint();
		} else if (backgroundTaskMonitor != null) {
			this.backgroundTaskPanel = backgroundTaskMonitor;
			add(backgroundTaskPanel, BorderLayout.SOUTH);
			repaint();
		}
	}


	private JScrollPane scrollPane_Publication;
	private JPanel panelPubId;
	private JTree treePubId;
	
	/**
	 * Cluster view
	 */
	private JPanel clusterPanel = new JPanel();
	private JLabel cluster;

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

		/* Build cluster view panel */
		this.setStaticClusterView();
		clusterPanel.setVisible(false);

		mainPanel.add(voidPanel);
		mainPanel.add(proteinPanel);
		mainPanel.add(interactionPanel);
		mainPanel.add(clusterPanel);
		
		addComponentListener(new ComponentAdapter() {
	        public void componentResized(ComponentEvent evt) {
	        	if (proteinPanel.isVisible()) {
		            setProteinView(myRow);
	        	}
	        	if (interactionPanel.isVisible()) {
	        		setInteractionView(myRow);
	        	}
	        }
		});
	}
	
	public void setRow(CyRow row) {
		this.myRow = row;
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
	 * Build the cluster view. When one or several nodes are clicked and sharing a cluster.
	 */
	private void setStaticClusterView() {
		clusterPanel.setLayout(new MigLayout("hidemode 3", "[grow,center]", "[80px:n,center][]"));
		
		final JLabel lblCluster = new JLabel("PMB cluster");
		lblCluster.setFont(new Font("Tahoma", Font.PLAIN, 22));
		clusterPanel.add(lblCluster, "cell 0 1,alignx left");

		cluster = NONE_LABEL();
		clusterPanel.add(cluster, "cell 0 2");
		
		clusterPanel.setVisible(true);
	}
	
	public void setClusterView(String c) {
		this.setCluster(c);
		
		this.showClusterView();
	}

	/**
	 * Build the protein summary view. Display titles and default value (they
	 * may not be needed).
	 */
	private void setStaticProteinView() {
		proteinPanel.setLayout(new MigLayout("insets 5, hidemode 3", "[][70px:70px:70px,grow,right]10[grow]", "[][][][][][][][]"));

		// PROTEIN NAME
		ptnName = new JLabel("Protein View");
		proteinPanel.add(ptnName, "cell 1 0 2 1,grow");
		
		// UNIPROT ID
		final JLabel lblUniprotId = new JLabel("Uniprot ID:");
		proteinPanel.add(lblUniprotId, "cell 1 1,alignx left");
		proteinId = NONE_LABEL();
		proteinPanel.add(proteinId, "cell 2 1");

		// EC NUMBER
		final JLabel lblEcNumber = new JLabel("EC Number:");
		proteinPanel.add(lblEcNumber, "flowx,cell 1 2,alignx left");
		ecNum = NONE_LABEL();
		proteinPanel.add(ecNum, "cell 2 2");

		// ORGANISM
		final JLabel lblNewLabel = new JLabel("Organism:");
		proteinPanel.add(lblNewLabel, "cell 1 3,alignx left");
		proteinOrganism = NONE_LABEL();
		proteinPanel.add(proteinOrganism, "cell 2 3");

		// GENE NAME
		final JLabel lblGeneName = new JLabel("Gene name:");
		proteinPanel.add(lblGeneName, "flowx,cell 1 4,alignx left");
		geneName = NONE_LABEL();
		proteinPanel.add(geneName, "cell 2 4");
		
		// CLUSTER NAME
		lblCluster = new JLabel("PMB cluster:");
		proteinPanel.add(lblCluster, "cell 1 5,alignx left");
		goCluster = NONE_LABEL();
		proteinPanel.add(goCluster, "cell 2 5");

		// SYNONYMS (position in panel decided case by case)
		scrollPane_Synonyms = new JScrollPane();
		scrollPane_Synonyms.setOpaque(false);
		scrollPane_Synonyms.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Synonyms", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelSynonyms = new JPanel();
		scrollPane_Synonyms.setViewportView(panelSynonyms);
		panelSynonyms.setBorder(null);
		panelSynonyms.setLayout(new BoxLayout(panelSynonyms, BoxLayout.Y_AXIS));
		treeSynonyms = new JTree();
		panelSynonyms.add(treeSynonyms);

		// ORTHOLOGS (position in panel decided case by case)
		scrollPane_Orthologs = new JScrollPane();
		scrollPane_Orthologs.setOpaque(false);
		scrollPane_Orthologs.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Orthologs", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelOrthologs = new JPanel();
		scrollPane_Orthologs.setViewportView(panelOrthologs);
		panelOrthologs.setBorder(null);
		panelOrthologs.setLayout(new BoxLayout(panelOrthologs, BoxLayout.Y_AXIS));
		treeOrthologs = new JTree();
		panelOrthologs.add(treeOrthologs);

		// GENE ONTOLOGY (position in panel decided case by case)
		scrollPane_GO = new JScrollPane();
		scrollPane_GO.setOpaque(false);
		scrollPane_GO.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Gene Ontology", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		final JPanel panel_GO = new JPanel();
		scrollPane_GO.setViewportView(panel_GO);
		panel_GO.setBorder(null);
		panel_GO.setLayout(new BorderLayout(0, 0));
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

			proteinPanel.add(lblReviewed, "cell 0 0,alignx right,aligny center");
			proteinPanel.add(lblExtLinkUniprot, "cell 0 1,alignx right,aligny center");
			proteinPanel.add(lblExtLinkECnum, "cell 0 2,alignx right,aligny center");
			proteinPanel.add(lblExtLinkOrganism, "cell 0 3,alignx right,aligny center");
			proteinPanel.add(lblExtLinkGenename, "cell 0 4,alignx right,aligny center");
		}

	}

	/**
	 * Add CyRow data to the protein panel.
	 * 
	 * @param row
	 */
	public void setProteinView(CyRow row) {
		// In the following code, you will see some calculation with the width variable
		// We once divide it by three, and then, sometimes we multiply it by two, sometimes not
		// All these operations are well studied
		// The interface design is a Science which requires rigor, accuracy and faith in numbers
		// Don't modify them. Ever.
		Integer width = getWidth() / 3;
		
		// PROTEIN NAME
		this.setPtnName(row.get("Protein_name", String.class), width*2);
		this.setReviewState(row.get("Reviewed", String.class).equalsIgnoreCase("true"));
		
		// UNIPROT ID
		this.setProteinId(row.get("Uniprot_id", String.class), width);
		
		// EC NUMBER
		final String ecNumber = row.get("Ec_number", String.class);
		this.setEcNumber(ecNumber != null ? ecNumber : "", width);
		
		// ORGANISM
		final String taxId = row.get("Tax_id", String.class);
		Organism org = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(Integer.parseInt(taxId));
		this.setProteinOrganism(org != null ? org.getScientificName() : null, taxId, width);

		// GENE NAME
		final String geneName = row.get("Gene_name", String.class);
		this.setGeneName(geneName != null ? geneName : "", taxId, width);

		int i = 5;
		
		// CLUSTER NAME
		final String goSlimGroupTerm = row.get("Go_slim_group_term", String.class);
		if (goSlimGroupTerm != null) {
			this.setGOCluster(goSlimGroupTerm, width);
			lblCluster.setVisible(true);
			goCluster.setVisible(true);
			i = i + 1;
		} else {
			lblCluster.setVisible(false);
			goCluster.setVisible(false);
		}
		
		// SYNONYMS
		final List<String> synonymGeneNames = row.getList("Synonym_gene_names", String.class);
		if (synonymGeneNames !=  null && !synonymGeneNames.isEmpty()) {
			proteinPanel.remove(scrollPane_Synonyms);
			proteinPanel.add(scrollPane_Synonyms, "cell 0 "+i+" 3 1,grow");
			this.setGeneNameSynonyms(synonymGeneNames, width);
			scrollPane_Synonyms.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_Synonyms.setVisible(false);
		}

		// ORTHOLOGS
		final List<String> orthologs = row.getList("Orthologs", String.class);
		if (orthologs != null && !orthologs.isEmpty()) {
			proteinPanel.add(scrollPane_Orthologs, "cell 0 "+i+" 3 1,grow");
			this.setOrthologs(orthologs, width*2);
			scrollPane_Orthologs.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_Orthologs.setVisible(false);
		}	
		
		// GENE ONTOLOGY
		final List<String> biologicalProcesses = row.getList("Biological_processes_hidden", String.class);
		final List<String> cellularComponents = row.getList("Cellular_components_hidden", String.class);
		final List<String> molecularFunctions = row.getList("Molecular_functions_hidden", String.class);
		if (!(biologicalProcesses != null && biologicalProcesses.isEmpty() &&
				cellularComponents != null && cellularComponents.isEmpty() &&
				molecularFunctions != null && molecularFunctions.isEmpty())) {
			proteinPanel.add(scrollPane_GO, "cell 0 "+i+" 3 1,grow");
			this.setOntology(biologicalProcesses, cellularComponents,
					molecularFunctions, width);
			scrollPane_GO.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_GO.setVisible(false);
		}

		this.showProteinView();
	}

	private void setStaticInteractionView() {
		interactionPanel.setLayout(new MigLayout("insets 5, hidemode 3", "[70px:70px:70px,grow,right]10[grow][]", "[][][][][][][][]"));

		// INTERACTOR A
		intAName = new JLabel("Interaction View");
		interactionPanel.add(intAName, "cell 0 0 2 1,grow");
		
		// INTERACTOR B
		intBName = new JLabel();
		interactionPanel.add(intBName, "cell 0 1 2 1,grow");
		
		// ORGANISM
		final JLabel lblInteractionOrganism = new JLabel("Organism:");
		interactionPanel.add(lblInteractionOrganism, "cell 0 2,alignx left");
		interactionOrganism = NONE_LABEL();
		interactionPanel.add(interactionOrganism, "cell 1 2");
		
		// DATABASES (position in panel decided case by case)
		scrollPane_Source = new JScrollPane();
		scrollPane_Source.setOpaque(false);
		scrollPane_Source.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Sources", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelSource = new JPanel();
		scrollPane_Source.setViewportView(panelSource);
		panelSource.setBorder(null);
		panelSource.setLayout(new BoxLayout(panelSource, BoxLayout.Y_AXIS));
		treeSource = new JTree();
		panelSource.add(treeSource);
		
		// INTERACTION TYPES (position in panel decided case by case)
		scrollPane_Type = new JScrollPane();
		scrollPane_Type.setOpaque(false);
		scrollPane_Type.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Interaction types", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelType = new JPanel();
		scrollPane_Type.setViewportView(panelType);
		panelType.setBorder(null);
		panelType.setLayout(new BoxLayout(panelType, BoxLayout.Y_AXIS));
		treeType = new JTree();
		panelType.add(treeType);
		
		// DETECTION METHODS (position in panel decided case by case)
		scrollPane_DetMeth = new JScrollPane();
		scrollPane_DetMeth.setOpaque(false);
		scrollPane_DetMeth.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Detection methods", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelDetMeth = new JPanel();
		scrollPane_DetMeth.setViewportView(panelDetMeth);
		panelDetMeth.setBorder(null);
		panelDetMeth.setLayout(new BoxLayout(panelDetMeth, BoxLayout.Y_AXIS));
		treeDetMeth = new JTree();
		panelDetMeth.add(treeDetMeth);
		
		// CONFIDENCE (position in panel decided case by case)
		scrollPane_Confidence = new JScrollPane();
		scrollPane_Confidence.setOpaque(false);
		scrollPane_Confidence.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Confidence", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelConfidence = new JPanel();
		scrollPane_Confidence.setViewportView(panelConfidence);
		panelConfidence.setBorder(null);
		panelConfidence.setLayout(new BoxLayout(panelConfidence, BoxLayout.Y_AXIS));
		treeConfidence = new JTree();
		panelConfidence.add(treeConfidence);
		
		// PUBLICATIONS (position in panel decided case by case)
		scrollPane_Publication = new JScrollPane();
		scrollPane_Publication.setOpaque(false);
		scrollPane_Publication.setBorder(new TitledBorder(new LineBorder(new Color(180, 180, 180), 1, true), "Publications", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		panelPubId = new JPanel();
		scrollPane_Publication.setViewportView(panelPubId);
		panelPubId.setBorder(null);
		panelPubId.setLayout(new BoxLayout(panelPubId, BoxLayout.Y_AXIS));
		

	}

	public void setInteractionView(CyRow row) {
		Integer width = getWidth() / 3;
		
		// INTERACTOR A
		this.setIntAName(row.get("Protein_name_A", String.class), width*2);
		
		// INTERACTOR B
		this.setIntBName(row.get("Protein_name_B", String.class), width*2);

		// ORGANISM
		String taxId = row.get("Tax_id", String.class);
		if(taxId != null) {
			Organism org = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(Integer.parseInt(taxId));
			this.setInteractionOrganism(org != null ? org.getScientificName(): null, taxId, width);
		}

		int i = 3;

		// DATABASES
		final List<String> source = row.getList("Source", String.class);
		if (source != null && !source.isEmpty()) {
			interactionPanel.add(scrollPane_Source, "cell 0 "+i+" 3 1,grow");
			this.setSource(source, width);
			scrollPane_Source.setVisible(true);		
			i = i + 1;
		} else {
			scrollPane_Source.setVisible(false);
		}
		
		// INTERACTION TYPE
		final List<String> type = row.getList("Type", String.class);
		if (type != null && !type.isEmpty()) {
			interactionPanel.add(scrollPane_Type, "cell 0 "+i+" 3 1,grow");
			this.setType(type, width*2);
			scrollPane_Type.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_Type.setVisible(false);
		}
		
		// DETECTION METHODS
		final List<String> detmethod = row.getList("Detmethod", String.class);
		if (detmethod != null && !detmethod.isEmpty()) {
			interactionPanel.add(scrollPane_DetMeth, "cell 0 "+i+" 3 1,grow");
			this.setDetMeth(detmethod, width*2);
			scrollPane_DetMeth.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_DetMeth.setVisible(false);
		}
				
		// CONFIDENCE
		final List<String> confidence = row.getList("Confidence", String.class);
		if (confidence != null && !confidence.isEmpty()) {
			interactionPanel.add(scrollPane_Confidence, "cell 0 "+i+" 3 1,grow");
			this.setConfidence(confidence, width*2);
			scrollPane_Confidence.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_Confidence.setVisible(false);
		}
		
		// PUBLICATIONS
		final List<String> pubid = row.getList("Pubid", String.class);
		if (pubid != null && !pubid.isEmpty()) {
			interactionPanel.add(scrollPane_Publication, "cell 0 "+i+" 3 1,grow");
			this.setPubId(pubid);
			scrollPane_Publication.setVisible(true);
			i = i + 1;
		} else {
			scrollPane_Publication.setVisible(false);
		}

		this.showInteractionView();
	}
	
	public void showInteractionView() {
		voidPanel.setVisible(false);
		proteinPanel.setVisible(false);
		interactionPanel.setVisible(true);
		clusterPanel.setVisible(false);

		this.repaint();
	}
	
	/**
	 * Display the default view. When no node or 2+ nodes are selected.
	 */
	public void showDefaultView() {
		proteinPanel.setVisible(false);
		interactionPanel.setVisible(false);
		voidPanel.setVisible(true);
		clusterPanel.setVisible(false);

		this.repaint();
	}

	/**
	 * Display the protein summary view. When 1 single protein is selected.
	 */
	public void showProteinView() {
		voidPanel.setVisible(false);
		interactionPanel.setVisible(false);
		proteinPanel.setVisible(true);
		clusterPanel.setVisible(false);

		this.repaint();
	}
	
	public void showClusterView() {
		voidPanel.setVisible(false);
		interactionPanel.setVisible(false);
		proteinPanel.setVisible(false);
		clusterPanel.setVisible(true);

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
	 * @param i 
	 */
	public void setPtnName(String ptnName, Integer width) {
		if (!ptnName.isEmpty()) {
			if (ptnName.length() > 60) {
				this.ptnName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, ptnName.substring(0, 57) + "..."));
			} else {
				this.ptnName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, ptnName));
			}
			this.ptnName.setToolTipText(ptnName);

		} else {
			this.ptnName.setFont(NONE_FONT);
			this.ptnName.setText("none");
		}
	}

	/**
	 * Set the Tax ID
	 * @param width 
	 */
	private void setInteractionOrganism(String name, String taxId, Integer width) {
		String text = formatOrganism(name, taxId);
		this.intOrgaText = text;
		if (!text.isEmpty()) {
			this.interactionOrganism.setFont(STD_FONT);
			this.interactionOrganism.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, text));
			try {
				this.lblExtLinkOrganism.setUri(new URI("http://www.uniprot.org/taxonomy/" + taxId));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkOrganism.setVisible(true);
		} else {
			this.lblExtLinkOrganism.setVisible(false);
			this.interactionOrganism.setFont(NONE_FONT);
			this.interactionOrganism.setText("none");
		}
	}
	
	/**
	 * Set pubId
	 */
	private void setPubId(List<String> pubId) {
		this.panelPubId.removeAll();
		
		//System.out.println(pubId);

		for (String str : pubId) {
			
			String[] parts = str.split(":");
			if (parts[0].equals("pubmed")){ // We keep only Pubmed IDs to remove duplicates (the other IDs links to the same publications but do not allow URL creation) 
				//System.out.println("pubmed");

				JPanel publication = new JPanel();
				publication.setLayout(new BoxLayout(publication, BoxLayout.LINE_AXIS));
				
				publication.add(new JLabel("• "+ str));
				publication.add(Box.createRigidArea(new Dimension(5,0)));
				JHyperlinkLabel lblExtLinkPubMed;
				lblExtLinkPubMed = new JHyperlinkLabel(openBrowser);
				lblExtLinkPubMed.setVisible(false);
				lblExtLinkPubMed.setIcon(ICN_EXTERNAL_LINK);
				lblExtLinkPubMed.makeClickable();
				publication.add(lblExtLinkPubMed);
			
			
				
				try {
					lblExtLinkPubMed.setVisible(true);

					lblExtLinkPubMed.setToolTipText("View on PubMed Publication");
					lblExtLinkPubMed.setUri(new URI("http://www.ncbi.nlm.nih.gov/pubmed/?term=" + parts[1]));
					
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				this.panelPubId.add(publication);
				publication.setAlignmentX(LEFT_ALIGNMENT);
			}
		}
	}
	
	/**
	 * Set source
	 * 
	 * @param source
	 * @param width 
	 */
	private void setSource(List<String> source, Integer width) {
		this.panelSource.removeAll();
		
		// Construct a map to get Database URL from name
		PsicquicRegistry reg = PsicquicRegistry.getInstance();
		LinkedHashMap<String, String> getDbUrl = new LinkedHashMap<String, String>();
		try {
			for (PsicquicService db : reg.getServices()) {
				getDbUrl.put(db.getName().toLowerCase(), db.getOrganizationUrl());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Construc list and links
		for (String str : source) {
			
			JPanel database = new JPanel();
			database.setLayout(new BoxLayout(database, BoxLayout.LINE_AXIS));
			
			database.add(new JLabel("• "+ str));
			database.add(Box.createRigidArea(new Dimension(5,0)));
			JHyperlinkLabel lblExtLinkDb;
			lblExtLinkDb = new JHyperlinkLabel(openBrowser);
			lblExtLinkDb.setVisible(false);
			lblExtLinkDb.setIcon(ICN_EXTERNAL_LINK);
			lblExtLinkDb.makeClickable();
			database.add(lblExtLinkDb);
			
			if (getDbUrl.containsKey(str.toLowerCase())){ 
				try {
					lblExtLinkDb.setVisible(true);

					lblExtLinkDb.setToolTipText("Visit website");
					lblExtLinkDb.setUri(new URI(getDbUrl.get(str.toLowerCase())));
					
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			this.panelSource.add(database);
			database.setAlignmentX(LEFT_ALIGNMENT);
		}
	}
	
	/**
	 * Set Type
	 * 
	 * @param type
	 */
	private void setType(List<String> type, Integer width) {
		this.panelType.removeAll();
		for (String str : type) {
			this.panelType.add(new JLabel(String.format("<html><div style=\"width:%dpx;\"> •   %s</div><html>", width, str)));
		}
	}
	
	/**
	 * Set Detection methods
	 * 
	 * @param detmeth
	 */
	private void setDetMeth(List<String> detmeth, Integer width) {
		this.panelDetMeth.removeAll();
		for (String str : detmeth) {
			this.panelDetMeth.add(new JLabel(String.format("<html><div style=\"width:%dpx;\"> •   %s</div><html>", width, str)));
		}
	}
	
	/**
	 * Set confidence
	 */
	private void setConfidence(List<String> confidence, Integer width) {
		this.panelConfidence.removeAll();
		for (String str : confidence) {
			this.panelConfidence.add(new JLabel(String.format("<html><div style=\"width:%dpx;\"> •   %s</div><html>", width, str)));
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
	 */
	private void setGeneName(String geneName, String taxId, Integer width) {
		if (!geneName.isEmpty()) {
			this.geneName.setFont(STD_FONT);
			this.geneName.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, geneName));
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
	 * @param i 
	 */
	private void setProteinId(String uniprotId, Integer width) {
		if (!uniprotId.isEmpty()) {
			this.proteinId.setFont(STD_FONT);
			this.proteinId.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, uniprotId));
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
	private void setEcNumber(String ecNumber, Integer width) {
		if (!ecNumber.isEmpty() && ecNumber != null) {
			this.ecNum.setFont(STD_FONT);
			this.ecNum.setText(ecNumber);
			this.ecNum.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, ecNumber));
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
	public String getProteinOrganism() {
		return proteinOrganism.getText();
	}

	/**
	 * Set the organism name and taxonomic ID
	 */
	private void setProteinOrganism(String name, String taxId, Integer width) {
		String text = formatOrganism(name, taxId);
		if (!text.isEmpty()) {
			this.proteinOrganism.setFont(STD_FONT);
			this.proteinOrganism.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, text));
			try {
				this.lblExtLinkOrganism.setUri(new URI("http://www.uniprot.org/taxonomy/" + taxId));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.lblExtLinkOrganism.setVisible(true);
		} else {
			this.lblExtLinkOrganism.setVisible(false);
			this.proteinOrganism.setFont(NONE_FONT);
			this.proteinOrganism.setText("none");
		}
	}

	private String formatOrganism(String name, String taxId) {
		if (name == null || name.isEmpty()) {
			return  "[" + taxId + "]";
		} else {
			return  name + " [" + taxId + "]";
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
	private void setGeneNameSynonyms(List<String> synonyms, Integer width) {
		this.panelSynonyms.removeAll();
		
		if (!synonyms.isEmpty()) {
			for (String str : synonyms) {
				this.panelSynonyms.add(new JLabel(String.format("<html><div style=\"width:%dpx;\"> •   %s</div><html>", width, str)));
			}
		}
		else {
			this.panelSynonyms.add(new JLabel(""));
		}
		

	}

	/**
	 * Set orthologs
	 * 
	 * @param orthologs
	 */
	private void setOrthologs(List<String> orthologs, Integer width) {
		this.panelOrthologs.removeAll();
		
		for (String str : orthologs) {
			JsonObject json = JsonObject.readFrom(str);
			this.panelOrthologs.add(new JLabel(String.format("<html><div style=\"width:%dpx;\"> •   %s [%d]</div><html>", width, json.get("uniProtId").asString(), json.get("organism").asInt())));
		}

	}

	/**
	 * Get orthologs
	 * 
	 * @return
	 */
	public List<Protein> getOrthologs() {
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
	private void setOntology(final List<String> biologicalProcess, final List<String> cellularComponent, final List<String> molecularFunction, Integer width) {
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
	 */
	public void setIntAName(String intAName, Integer width) {
		if (intAName != null &&
				!intAName.isEmpty()) {
			if (intAName.length() > 60) {
				this.intAName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, intAName.substring(0, 57) + "..."));
			} else {
				this.intAName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, intAName));
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
	 */
	public void setIntBName(String intBName, Integer width) {
		if (intBName != null &&
				!intBName.isEmpty()) {
			if (intBName.length() > 60) {
				this.intBName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, intBName.substring(0, 57) + "..."));
			} else {
				this.intBName.setText(String.format("<html><div style=\"width:%dpx;font-size:18px;\">%s</div><html>", width, intBName));
			}
			this.intBName.setToolTipText(intBName);

		} else {
			this.intBName.setFont(NONE_FONT);
			this.intBName.setText("none");
		}
	}
	
	public void setCluster(String c) {
		this.cluster.setText(c);
	}
	
	public void setGOCluster(String c, Integer width) {
		this.goCluster.setText(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", width, c));
	}


}

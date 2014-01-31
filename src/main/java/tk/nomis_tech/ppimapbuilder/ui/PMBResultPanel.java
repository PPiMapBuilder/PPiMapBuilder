package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import tk.nomis_tech.ppimapbuilder.ui.panel.ResultPanel;

public class PMBResultPanel extends ResultPanel implements CytoPanelComponent {

	private final String title = "PPiMapBuilder";
	private Font titleFont = new Font("Lucida Grande", Font.BOLD, 18);

	private JPanel topPanel;
	private JScrollPane scrollPane;

	public PMBResultPanel() {
		super();
		setPreferredSize(new Dimension(320, 700));
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

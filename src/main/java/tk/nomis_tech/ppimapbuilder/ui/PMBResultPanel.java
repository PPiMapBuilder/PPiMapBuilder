package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import tk.nomis_tech.ppimapbuilder.ui.panel.ResultPanel;

public class PMBResultPanel extends ResultPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6177714676582301901L;
	private final String title = "PPiMapBuilder";

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

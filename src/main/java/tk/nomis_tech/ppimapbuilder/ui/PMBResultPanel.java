package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class PMBResultPanel extends JPanel implements CytoPanelComponent {
	
	private final String title = "PPiMapBuilder";
	
	public PMBResultPanel() {
		setPreferredSize(new Dimension(350, 1300));
		setMinimumSize(new Dimension(250, 300));
		JScrollPane scrollpane = new JScrollPane(this);
		setAutoscrolls(true);
		JLabel lbXYZ = new JLabel("This is my PMB Control Panel");
		lbXYZ.setToolTipText("coucou tu veux voir ma mite?");
		lbXYZ.setFont(new Font(null, Font.ITALIC, 25));

		
		this.add(lbXYZ);
		this.setVisible(true);
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

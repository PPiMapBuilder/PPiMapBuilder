package tk.nomis_tech.ppimapbuilder.ui.util;

import java.awt.Image;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;

import org.cytoscape.util.swing.OpenBrowser;

/**
 * PPiMapBuilder logo icon label
 */
public class LogoIcon extends JHyperlinkLabel{
	private static final long serialVersionUID = 1L;
    
	public LogoIcon(OpenBrowser openBrowser) {
		super(openBrowser);
		
		try {
			ImageIcon icon = new ImageIcon(LogoIcon.class.getResource("/pmblogo.png"));
			
			
			Image img = icon.getImage();  
			Image newimg = img.getScaledInstance(140, 182,  java.awt.Image.SCALE_SMOOTH);  
			ImageIcon newIcon = new ImageIcon(newimg);
			setIcon(newIcon);
		} catch (Exception e) {
			setText("[LOGO]");
		}
		
		this.setToolTipText("Access to source code");
		this.makeClickable();
		
		
		try {
			this.setUri(new URI("http://github.com/PPiMapBuilder/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}

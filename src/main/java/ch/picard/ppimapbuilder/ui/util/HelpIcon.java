package ch.picard.ppimapbuilder.ui.util;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Help JLabel Icon with tool tip help
 */
public class HelpIcon extends JLabel{
	private static final long serialVersionUID = 1L;

	public HelpIcon(String message) {
		super();
		try {
			setIcon(new ImageIcon(HelpIcon.class.getResource("help.png")));
		} catch (Exception e) {
			setText("[?]");
		}
		setToolTipText(message);
	}
}

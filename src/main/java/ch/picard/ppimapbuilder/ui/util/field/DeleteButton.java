package ch.picard.ppimapbuilder.ui.util.field;

import javax.swing.*;
import java.awt.*;

public class DeleteButton extends JButton {

	public DeleteButton() {
		ImageIcon icon = new ImageIcon(ListDeletableItem.class.getResource("delete.png"));
		setIcon(icon);
		Dimension iconDim = new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2);
		setMinimumSize(iconDim);
		setMaximumSize(iconDim);
		setPreferredSize(iconDim);
		setContentAreaFilled(false);
		setBorder(BorderFactory.createEmptyBorder());
	}
}

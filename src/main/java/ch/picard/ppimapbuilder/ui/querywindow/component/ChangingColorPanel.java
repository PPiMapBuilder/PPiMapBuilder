package ch.picard.ppimapbuilder.ui.querywindow.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ChangingColorPanel extends JPanel implements ChangingColorComponent {

	private static final long serialVersionUID = 1L;

	private final Color focusColor;
	private final Color blurColor;
	private final Border focusBorder;
	private final Border blurBorder;

	public ChangingColorPanel(ChangingColorComponentUpdater updater, Color focusColor, Color blurColor, Border focusBorder, Border blurBorder) {
		updater.add(this);
		this.focusColor = focusColor;
		this.blurColor = blurColor;
		this.focusBorder = focusBorder;
		this.blurBorder = blurBorder;
		focusColorChange();
	}

	@Override
	public void focusColorChange() {
		boolean repaint = false;
		if (focusBorder != null) {
			repaint = true;
			setBorder(focusBorder);
		}
		if (focusColor != null) {
			repaint = true;
			setBackground(focusColor);
		}
		if (repaint)
			repaint();
	}

	@Override
	public void blurColorChange() {
		boolean repaint = false;
		if (blurBorder != null) {
			repaint = true;
			setBorder(blurBorder);
		}
		if (blurColor != null) {
			repaint = true;
			setBackground(blurColor);
		}
		if (repaint)
			repaint();
	}
}

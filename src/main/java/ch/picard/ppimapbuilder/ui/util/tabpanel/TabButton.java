package ch.picard.ppimapbuilder.ui.util.tabpanel;

import ch.picard.ppimapbuilder.ui.util.FocusPropagator;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class TabButton<T extends TabContent> extends JButton implements FocusListener {

	private final boolean first;
	private boolean active = false;
	private boolean focused = true;

	private Border bottomBorder = new MatteBorder(0, 0, 1, 0, PMBUIStyle.defaultBorderColor);

	public TabButton(final T panel, boolean first) {
		this.first = first;
		setText(panel.getComponent().getName());
		updateColors();
		setOpaque(true);
	}

	public void setActive(boolean active) {
		boolean wasActive = this.active;
		this.active = active;
		if ((wasActive && !active) || (!wasActive && active))
			updateColors();
	}

	private void updateColors() {
		if (active) {
			if (focused) setBackground(PMBUIStyle.focusActiveTabColor);
			else setBackground(PMBUIStyle.blurActiveTabColor);

			setBorder(null);
		} else {
			if (focused) setBackground(PMBUIStyle.focusInactiveTabColor);
			else setBackground(PMBUIStyle.blurInactiveTabColor);

			setBorder(bottomBorder);
		}

		if (!first) setBorder(new CompoundBorder(
				new MatteBorder(0, 1, 0, 0, PMBUIStyle.defaultBorderColor),
				getBorder()
		));

		repaint();
	}

	@Override
	public void focusGained(FocusEvent e) {
		boolean wasBlurred = !focused;
		focused = true;
		if (wasBlurred)
			updateColors();
	}

	@Override
	public void focusLost(FocusEvent e) {
		boolean wasFocused = focused;
		focused = false;
		if (wasFocused)
			updateColors();
	}
}
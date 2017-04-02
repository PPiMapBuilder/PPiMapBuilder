package ch.picard.ppimapbuilder.ui.util;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

public class FocusPropagator implements WindowFocusListener {

	private final List<FocusListener> focusPropagatorListeners;
	private final Component parent;

	public FocusPropagator(Component parent) {
		this.parent = parent;
		focusPropagatorListeners = new ArrayList<FocusListener>();
	}

	public FocusListener add(FocusListener changingColorComponent) {
		focusPropagatorListeners.add(changingColorComponent);
		return changingColorComponent;
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		for (FocusListener focusPropagatorListener : focusPropagatorListeners)
			focusPropagatorListener.focusGained(new FocusEvent(parent, FocusEvent.FOCUS_GAINED));
		parent.repaint();
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		for (FocusListener focusPropagatorListener : focusPropagatorListeners)
			focusPropagatorListener.focusLost(new FocusEvent(parent, FocusEvent.FOCUS_LOST));
		parent.repaint();
	}
}

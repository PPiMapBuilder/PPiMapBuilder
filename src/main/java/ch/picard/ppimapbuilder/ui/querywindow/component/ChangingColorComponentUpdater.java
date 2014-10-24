package ch.picard.ppimapbuilder.ui.querywindow.component;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

public class ChangingColorComponentUpdater implements WindowFocusListener {

	private final List<ChangingColorComponent> changingColorComponents;

	public ChangingColorComponentUpdater() {
		changingColorComponents = new ArrayList<ChangingColorComponent>();
	}

	protected  <T extends ChangingColorComponent> T add(T changingColorComponent) {
		changingColorComponents.add(changingColorComponent);
		return changingColorComponent;
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		for (ChangingColorComponent changingColorComponent : changingColorComponents)
			changingColorComponent.focusColorChange();
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		for (ChangingColorComponent changingColorComponent : changingColorComponents)
			changingColorComponent.blurColorChange();
	}
}

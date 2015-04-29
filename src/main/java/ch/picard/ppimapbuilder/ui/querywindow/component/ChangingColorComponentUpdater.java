/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
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

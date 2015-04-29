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
    
package ch.picard.ppimapbuilder.ui.util;

import javax.swing.border.*;
import java.awt.*;

public class PMBUIStyle {

	public static final Color defaultBorderColor = new Color(154, 154, 154);
	public static final Border defaultComponentBorder = new LineBorder(defaultBorderColor, 1);
	public static final CompoundBorder fancyPanelBorder = new CompoundBorder(
			// Outside border 1px bottom light color
			new MatteBorder(0, 0, 1, 0, new Color(255, 255, 255)),
			// Border all around panel 1px dark grey
			defaultComponentBorder
	);
	public static final CompoundBorder fancyPanelBorderWithPadding = new CompoundBorder(
			PMBUIStyle.fancyPanelBorder,
			new EmptyBorder(5, 5, 5, 5)
	);
	public static final Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
}

package ch.picard.ppimapbuilder.ui.util;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 *
 */
public class PMBUIStyle {

	private static Color defaultPanelColor = UIManager.getColor("Panel.background");
	private static float hsbVals[] = Color.RGBtoHSB(
			defaultPanelColor.getRed(),
			defaultPanelColor.getGreen(),
			defaultPanelColor.getBlue(),
			null
	);

	public static final Color blurActiveTabColor = Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2] *= 0.97f);
	public static final Color blurInactiveTabColor = Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2] *= 0.95f);

	public static final Color focusActiveTabColor = Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2] *= 0.97f);
	public static final Color focusInactiveTabColor = Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2] *= 0.84f);

	public static final Color defaultBorderColor = Color.getHSBColor(hsbVals[0], hsbVals[1], hsbVals[2] *= 0.94f);

	public static final Border defaultComponentBorder = new LineBorder(defaultBorderColor, 1);
	public static final Border fancyPanelBorder = new CompoundBorder(
			// Outside border 1px bottom light color
			new MatteBorder(0, 0, 1, 0, Color.WHITE),
			// Border all around panel 1px dark grey
			defaultComponentBorder
	);
	public static final Border emptyBorder = new EmptyBorder(0, 0, 0, 0);

}

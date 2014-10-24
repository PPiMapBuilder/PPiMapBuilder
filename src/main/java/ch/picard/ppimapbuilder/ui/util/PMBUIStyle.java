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

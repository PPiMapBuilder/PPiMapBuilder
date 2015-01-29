package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CustomSplitPane extends JSplitPane implements FocusListener {

	private static final long serialVersionUID = 1L;

	private final Border focusBorder;
	private final Border blurBorder;
	private final Color focusColor;
	private final Color blurColor;

	public CustomSplitPane(Color focusColor, Color blurColor) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);

		setUI(new BasicSplitPaneUI(){
			@Override
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {
					@Override
					public void paint(Graphics g) {
						super.paint(g);
						//g.setColor(bgColor);
						//g.fillRect(0, 0, getSize().width, getSize().height);

						Graphics2D g2d = (Graphics2D) g;
						int h = 12;
						int w = 2;
						int x = (getWidth() - w) / 2;
						int y = (getHeight() - h) / 2;
						g2d.setColor(PMBUIStyle.defaultBorderColor);
						g2d.drawOval(x, y, w, h);
					}
				};
			}
		});

		setBorder(focusBorder = new MatteBorder(5, 5, 5, 5, focusColor));
		blurBorder =  new MatteBorder(5, 5, 5, 5, blurColor);

		setBackground(this.focusColor = focusColor);
		this.blurColor = blurColor;

		setDividerSize(5);
	}

	@Override
	public void focusGained(FocusEvent e) {
		setBorder(focusBorder);
		setBackground(focusColor);
	}

	@Override
	public void focusLost(FocusEvent e) {
		setBorder(blurBorder);
		setBackground(blurColor);
	}
}

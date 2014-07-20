package ch.picard.ppimapbuilder.ui.settingwindow.component.panel;

import javax.swing.*;
import java.awt.*;

public class TabPanel extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	public TabPanel(TabContentPanel ... panels) {
		super(JTabbedPane.TOP);

		for(TabContentPanel panel: panels)
			add(panel, panel.getName());
	}

	public static abstract class TabContentPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public TabContentPanel(LayoutManager layout, String name) {
			super(layout);
			setBorder(BorderFactory.createEmptyBorder());
			setOpaque(false);
			setName(name);
		}

		@Override
		public Component add(Component comp) {
			if(comp instanceof JComponent) ((JComponent) comp).setOpaque(false);
			return super.add(comp);
		}

		@Override
		public void add(Component comp, Object constraints) {
			if(comp instanceof JComponent) ((JComponent) comp).setOpaque(false);
			super.add(comp, constraints);
		}

		@Override
		public void add(Component comp, Object constraints, int index) {
			if(comp instanceof JComponent) ((JComponent) comp).setOpaque(false);
			super.add(comp, constraints, index);
		}

		public abstract void resetUI();
	}
}
package tk.nomis_tech.ppimapbuilder.ui.settingwindow.panel;

import javax.swing.*;

public class TabPanel extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	public TabPanel(TabContentPanel ... panels) {
		super(JTabbedPane.TOP);

		for(TabContentPanel panel: panels)
			add(panel, panel.getName());
	}
}
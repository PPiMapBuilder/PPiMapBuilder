package ch.picard.ppimapbuilder.ui.settingwindow.component.panel;

import ch.picard.ppimapbuilder.ui.util.ListDeletableItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GOSlimSettingPanel extends TabPanel.TabContentPanel {

	private static final long serialVersionUID = 1L;

	private final ListDeletableItem goSlimListPanel;

	public GOSlimSettingPanel() {
		super(new BorderLayout(), "GO slim");

		setBorder(new EmptyBorder(5, 5, 5, 5));

		final JLabel lblSourceDatabases = new JLabel("Preferred GO slim:");
		add(lblSourceDatabases, BorderLayout.NORTH);

		this.goSlimListPanel = new ListDeletableItem();
		add(goSlimListPanel.getComponent(), BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.SOUTH);

		JButton addGOslimButton = new JButton("Add from OBO file");
		bottomPanel.add(addGOslimButton);
	}

}

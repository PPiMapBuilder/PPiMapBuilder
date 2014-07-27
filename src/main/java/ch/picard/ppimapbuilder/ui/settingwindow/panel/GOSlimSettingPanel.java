package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologySet;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.util.ListDeletableItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GOSlimSettingPanel extends TabPanel.TabContentPanel  {

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

		JButton addGOSlimButton = new JButton("Add from OBO file");
		bottomPanel.add(addGOSlimButton);
	}

	@Override
	public synchronized void resetUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				goSlimListPanel.removeAllRow();
				for (final GeneOntologySet set : PMBSettings.getInstance().getGoSlimList()) {
					goSlimListPanel.addRow(
						new ListDeletableItem.ListRow(set.getName())
							.addDeleteButton( new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									ArrayList<GeneOntologySet> list =
											new ArrayList<GeneOntologySet>(PMBSettings.getInstance().getGoSlimList());
									list.remove(set);
									PMBSettings.getInstance().setGoSlimList(list);
									resetUI();
								}
							})
					);
				}
				repaint();
			}
		});
	}

	@Override
	public void setVisible(boolean opening) {
		super.setVisible(opening);
		if (opening)
			resetUI();
	}

}

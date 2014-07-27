package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.ontology.GOSlimRepository;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologySet;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.util.ListDeletableItem;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GOSlimSettingPanel extends TabPanel.TabContentPanel {

	private static final long serialVersionUID = 1L;

	private final JPanel listPanel;

	private final ListDeletableItem goSlimListPanel;

	public GOSlimSettingPanel() {
		super(new BorderLayout(), "GO slim");
		setBorder(new EmptyBorder(5, 5, 5, 5));

		add(listPanel = new JPanel(new BorderLayout()), BorderLayout.CENTER);
		{
			listPanel.setOpaque(false);

			final JLabel lblSourceDatabases = new JLabel("Preferred GO slim:");
			listPanel.add(lblSourceDatabases, BorderLayout.NORTH);

			this.goSlimListPanel = new ListDeletableItem();
			listPanel.add(goSlimListPanel.getComponent(), BorderLayout.CENTER);

			JPanel bottomPanel = new JPanel();
			bottomPanel.setOpaque(false);
			listPanel.add(bottomPanel, BorderLayout.SOUTH);

			JButton addGOSlimButton = new JButton("Add from OBO file");
			bottomPanel.add(addGOSlimButton);
		}
	}

	@Override
	public synchronized void resetUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switchPanel(null);
				goSlimListPanel.removeAllRow();
				for (final GeneOntologySet set : PMBSettings.getInstance().getGoSlimList()) {
					ListDeletableItem.ListRow listRow = new ListDeletableItem.ListRow(set.getName())
							.addButton(newViewButton(set.getName()));

					if(!set.getName().equals(GeneOntologySet.DEFAULT)){
						listRow .addDeleteButton(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								ArrayList<GeneOntologySet> list =
										new ArrayList<GeneOntologySet>(PMBSettings.getInstance().getGoSlimList());
								list.remove(set);
								PMBSettings.getInstance().setGoSlimList(list);
								resetUI();
							}
						});
					}

					goSlimListPanel.addRow(listRow);
				}
				repaint();
			}
		});
	}

	private JButton newViewButton(final String goSlimName) {
		ImageIcon icon = new ImageIcon(getClass().getResource("view.png"));
		JButton button = new JButton(icon);
		Dimension iconDim = new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2);
		button.setMinimumSize(iconDim);
		button.setMaximumSize(iconDim);
		button.setPreferredSize(iconDim);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchPanel(new GOSlimVisualizer(goSlimName));
			}
		});
		return button;
	}

	@Override
	public void setVisible(boolean opening) {
		super.setVisible(opening);
		if (opening)
			resetUI();
	}

	private void switchPanel(JPanel panel) {
		removeAll();
		if (panel == null)
			add(listPanel, BorderLayout.CENTER);
		else
			add(panel, BorderLayout.CENTER);
		repaint();
	}

	private class GOSlimVisualizer extends JPanel {

		GOSlimVisualizer(String goSlimName) {
			setLayout(new BorderLayout());

			JTable table;
			{ // Load GO terms into JTable
				GeneOntologySet goSlim = GOSlimRepository.getInstance().getGOSlim(goSlimName);

				String[][] rows = new String[goSlim.size()][3];
				int i = 0;
				for (GeneOntologyTerm term : goSlim) {
					String[] row = rows[i++];
					row[0] = term.getIdentifier();
					row[1] = term.getCategory() != null ? term.getCategory().toString() : "";
					row[2] = term.getTerm();
				}
				Object columnNames[] = {"Identifier", "Category", "Term"};
				table = new JTable(rows, columnNames);

				table.getColumn("Identifier").setMaxWidth(110);
				table.getColumn("Identifier").setMinWidth(92);
				table.getColumn("Category").setMaxWidth(130);
				table.getColumn("Category").setMinWidth(110);
			}

			{ // Init the scroll panel
				JScrollPane scrollPane = new JScrollPane(table);
				scrollPane.setBorder(PMBUIStyle.fancyPanelBorder);
				table.setFillsViewportHeight(true);
				add(scrollPane);
				scrollPane.setOpaque(false);
				scrollPane.getViewport().setOpaque(false);
			}

			{ // Init bottom panel with back button
				JPanel bottomPanel = new JPanel();
				add(bottomPanel, BorderLayout.SOUTH);

				JButton backButton = new JButton("Back");
				bottomPanel.setOpaque(false);
				bottomPanel.add(backButton);

				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switchPanel(null);
					}
				});
			}
		}
	}
}

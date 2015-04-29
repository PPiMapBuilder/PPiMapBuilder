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
    
package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlim;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimLoaderTaskFactory;
import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.ListDeletableItem;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GOSlimSettingPanel extends TabPanel.TabContentPanel {

	private static final long serialVersionUID = 1L;

	private final JPanel listPanel;

	private final ListDeletableItem goSlimListPanel;
	private final SettingWindow settingWindow;

	public GOSlimSettingPanel(final SettingWindow settingWindow) {
		super(new BorderLayout(), "GO slim");
		setBorder(new EmptyBorder(5, 5, 5, 5));

		this.settingWindow = settingWindow;

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

			final GOSlimLoaderTaskFactory goSlimLoaderTaskFactory = new GOSlimLoaderTaskFactory();
			goSlimLoaderTaskFactory.setCallback(new AbstractTask() {
				@Override
				public void run(TaskMonitor monitor) throws Exception {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (goSlimLoaderTaskFactory.getError() == null) {
								settingWindow.newModificationMade();
								resetUI();
							} else {
								JOptionPane.showMessageDialog(settingWindow, goSlimLoaderTaskFactory.getError(), "Add OBO GO slim error", JOptionPane.ERROR_MESSAGE);
							}
							settingWindow.setVisible(true);
						}
					});
				}
			});

			addGOSlimButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					settingWindow.closeSilently();
					settingWindow.getTaskManager().execute(
							goSlimLoaderTaskFactory.createTaskIterator()
					);
				}
			});
		}
	}

	@Override
	public synchronized void resetUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switchPanel(null);
				goSlimListPanel.removeAllRow();

				List<GOSlim> goSlims = GOSlimRepository.getInstance().getGOSlims();
				Collections.sort(goSlims, new Comparator<GOSlim>() {
					@Override
					public int compare(GOSlim o1, GOSlim o2) {
						return o1.getName().equals(GOSlim.DEFAULT) ?
								-1 :
									o2.getName().equals(GOSlim.DEFAULT) ?
									1 :
									0;
					}
				});

				for (final GOSlim set : goSlims) {
					ListDeletableItem.ListRow listRow = new ListDeletableItem.ListRow(set.getName())
							.addButton(newViewButton(set.getName()));

					if (!set.getName().equals(GOSlim.DEFAULT)) {
						listRow.addDeleteButton(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								GOSlimRepository.getInstance().remove(set.getName());
								settingWindow.newModificationMade();
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
				GeneOntologyTermSet goSlim = GOSlimRepository.getInstance().getGOSlim(goSlimName);

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

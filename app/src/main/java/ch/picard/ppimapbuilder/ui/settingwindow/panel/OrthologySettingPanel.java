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

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader.InParanoidCacheLoaderTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.ui.util.label.InParanoidLogo;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;
import ch.picard.ppimapbuilder.util.io.FileUtil;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class OrthologySettingPanel extends JPanel implements TabContent {
	private final JLabel lblCacheSize;
	private final JLabel lblCachePercent;

	private final JButton btnClear;
	private final JButton btnLoad;
	private final SettingWindow settingWindow;
	private PMBProteinOrthologCacheClient cache;

	private final InParanoidCacheLoaderTaskFactory inParanoidCacheLoaderTaskFactory;

	public OrthologySettingPanel(OpenBrowser openBrowser, SettingWindow settingWindow) {
		super(new MigLayout("ins 5", "[grow, right]10[grow, left]", ""));
		setName("Orthology");
		this.settingWindow = settingWindow;
		this.cache = PMBProteinOrthologCacheClient.getInstance();

		add(new JLabel("Cache size :"));
		add(lblCacheSize = new JLabel("-"), "wrap");

		add(new JLabel("Cache loaded :"));
		add(lblCachePercent = new JLabel("-"), "wrap");

		add(btnClear = new JButton("Clear cache"), "w 220, center, sx 2, wrap");

		add(btnLoad = new JButton("Load cache"), "w 220, center, sx 2, wrap");

		add(new JLabel("Protein orthology data provided by :"), "center, sx 2, wrap");
		add(new InParanoidLogo(openBrowser), "center, sx 2");

		inParanoidCacheLoaderTaskFactory = new InParanoidCacheLoaderTaskFactory();

		initListeners();
	}

	private void initListeners() {
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					cache.empty();
					setActive(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingWindow.closeSilently();
				List<Organism> organisms = UserOrganismRepository.getInstance().getOrganisms();

				inParanoidCacheLoaderTaskFactory.setOrganisms(organisms);
				inParanoidCacheLoaderTaskFactory.setCallback(
						new AbstractTask() {
							@Override
							public void run(TaskMonitor monitor) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										if (inParanoidCacheLoaderTaskFactory.getMessage() != null) {
											JOptionPane.showMessageDialog(
													null,
													inParanoidCacheLoaderTaskFactory.getMessage(),
													"Orthology cache loading",
													JOptionPane.INFORMATION_MESSAGE
											);
										} else if (inParanoidCacheLoaderTaskFactory.getError() != null) {
											JOptionPane.showMessageDialog(
													null,
													inParanoidCacheLoaderTaskFactory.getError(),
													"Orthology cache loading error",
													JOptionPane.ERROR_MESSAGE
											);
										}
										setActive(true);
										settingWindow.setVisible(true);
									}
								});
							}
						}
				);

				settingWindow.getTaskManager().execute(inParanoidCacheLoaderTaskFactory.createTaskIterator());
			}
		});
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setActive(boolean active) {
		if (active) {
			String orthologCacheSize = FileUtil.getHumanReadableFileSize(
					PMBSettings.getInstance().getOrthologCacheFolder()
			);
			lblCacheSize.setText(orthologCacheSize);

			String orthologPercentUserOrg;
			try {
				double percentLoadedFromOrganisms = PMBProteinOrthologCacheClient.getInstance().getPercentLoadedFromOrganisms(
						UserOrganismRepository.getInstance().getOrganisms()
				);
				orthologPercentUserOrg =
						(percentLoadedFromOrganisms < 10.0 ?
								String.format("%,.2f", percentLoadedFromOrganisms) :
								String.valueOf((int) percentLoadedFromOrganisms))
								+ " %";
			} catch (IOException e) {
				orthologPercentUserOrg = "-";
			}
			lblCachePercent.setText(orthologPercentUserOrg);

			validate();
			repaint();
		}
	}
}

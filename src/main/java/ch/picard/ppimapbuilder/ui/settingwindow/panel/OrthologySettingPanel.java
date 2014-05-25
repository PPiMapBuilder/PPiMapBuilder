package ch.picard.ppimapbuilder.ui.settingwindow.panel;

import net.miginfocom.swing.MigLayout;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskIterator;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader.InParanoidCacheLoaderTaskFactory;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.ui.settingwindow.InParanoidLogo;
import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.util.FileUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class OrthologySettingPanel extends TabContentPanel {
	private final JLabel lblCacheSize;
	private final JLabel lblCachePercent;

	private final JButton btnClear;
	private final JButton btnLoad;
	private final SettingWindow settingWindow;
	private PMBProteinOrthologCacheClient cache;

	public OrthologySettingPanel(OpenBrowser openBrowser, SettingWindow settingWindow) {
		super(new MigLayout("ins 5", "[grow, right]10[grow, left]", ""), "Orthology");
		this.settingWindow = settingWindow;
		try {
			this.cache = PMBProteinOrthologCacheClient.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}

		add(new JLabel("Cache size :"));
		add(lblCacheSize = new JLabel("-"), "wrap");

		add(new JLabel("Cache loaded :"));
		add(lblCachePercent = new JLabel("-"), "wrap");

		add(btnClear = new JButton("Clear cache"), "w 220, center, sx 2, wrap");

		add(btnLoad = new JButton("Load cache"), "w 220, center, sx 2, wrap");

		add(new JLabel("Protein orthology data provided by :"), "center, sx 2, wrap");
		add(new InParanoidLogo(openBrowser), "center, sx 2");

		initListeners();
	}

	private void initListeners() {
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					cache.empty();
					OrthologySettingPanel.this.update();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<Organism> organisms = UserOrganismRepository.getInstance().getOrganisms();

				TaskIterator cacheLoaderTaskIterator = new InParanoidCacheLoaderTaskFactory(
						organisms,
						new Runnable() {
							@Override
							public void run() {
								OrthologySettingPanel.this.update();
							}
						}
				).createTaskIterator();

				settingWindow.getTaskManager().execute(cacheLoaderTaskIterator);
			}
		});
	}

	private void update() {
		new Thread() {
			public void run() {
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
			}
		}.start();
	}

	@Override
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		if (aFlag) update();
	}
}

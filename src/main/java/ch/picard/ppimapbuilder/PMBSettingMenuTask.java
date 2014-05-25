package ch.picard.ppimapbuilder;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;

/**
 * The interaction query menu
 */
public class PMBSettingMenuTask extends AbstractTask {

	private SettingWindow sw;

	public PMBSettingMenuTask(SettingWindow settingWindow) {
		this.sw = settingWindow;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					PsicquicRegistry reg = PsicquicRegistry.getInstance();
					sw.updateLists(reg.getServices());
					
					sw.getOrganismSettingPanel().updatePanSourceOrganism();
					sw.getOrganismSettingPanel().updateSuggestions();

					sw.setVisible(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
				}
			}
		});
	}

}

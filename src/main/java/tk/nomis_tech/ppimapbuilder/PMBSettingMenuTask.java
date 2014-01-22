package tk.nomis_tech.ppimapbuilder;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import tk.nomis_tech.ppimapbuilder.ui.SettingWindow;
import tk.nomis_tech.ppimapbuilder.util.PsicquicRegistry;

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
					PsicquicRegistry reg = new PsicquicRegistry();
					sw.updateLists(reg.getServices());

					sw.setVisible(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
				}
			}
		});
	}

}

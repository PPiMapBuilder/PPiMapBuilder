package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicRegistry;

import javax.swing.*;
import java.io.IOException;

/**
 * The interaction query menu
 */
public class PMBQueryMenuTask extends AbstractTask {

	private QueryWindow qw;

	public PMBQueryMenuTask(QueryWindow queryWindow) {
		this.qw = queryWindow;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					PsicquicRegistry reg = PsicquicRegistry.getInstance();
					qw.updateLists(reg.getServices(), OrganismRepository.getInstance().getListOrganism());

					qw.setVisible(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
				}
			}
		});
	}

}

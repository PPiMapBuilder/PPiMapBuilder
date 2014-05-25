package ch.picard.ppimapbuilder;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;

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
					qw.updateLists(reg.getServices(), UserOrganismRepository.getInstance().getOrganisms());

					qw.setVisible(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
				}
			}
		});
	}

}

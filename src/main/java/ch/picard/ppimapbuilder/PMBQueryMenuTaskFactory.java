package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import java.io.IOException;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBQueryMenuTaskFactory extends AbstractTaskFactory {

	private final QueryWindow queryWindow;

	public PMBQueryMenuTaskFactory(QueryWindow queryWindow) {
		this.queryWindow = queryWindow;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								PsicquicRegistry reg = PsicquicRegistry.getInstance();
								queryWindow.updateLists(
										reg.getServices(),
										UserOrganismRepository.getInstance().getOrganisms()
								);

								queryWindow.setVisible(true);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
							}
						}
					});
				}
			}
		);
	}

}

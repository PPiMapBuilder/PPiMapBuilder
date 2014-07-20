package ch.picard.ppimapbuilder;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBSettingMenuTaskFactory extends AbstractTaskFactory {

	private final SettingWindow settingWindow;

	public PMBSettingMenuTaskFactory(SettingWindow settingWindow) {
		this.settingWindow = settingWindow;
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
							settingWindow.setVisible(true);
						}
					});
				}
			}
		);
	}

}

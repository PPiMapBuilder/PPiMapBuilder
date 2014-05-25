package ch.picard.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBSettingMenuTaskFactory extends AbstractTaskFactory {

	private SettingWindow settingWindow;

	public PMBSettingMenuTaskFactory(SettingWindow settingWindow) {
		this.settingWindow = settingWindow;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBSettingMenuTask(settingWindow)
		);
	}

}

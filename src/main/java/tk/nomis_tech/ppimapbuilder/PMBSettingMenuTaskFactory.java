package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import tk.nomis_tech.ppimapbuilder.ui.settingwindow.SettingWindow;

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

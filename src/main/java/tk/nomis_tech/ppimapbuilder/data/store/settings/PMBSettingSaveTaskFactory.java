package tk.nomis_tech.ppimapbuilder.data.store.settings;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBSettingSaveTaskFactory extends AbstractTaskFactory {
	
	public PMBSettingSaveTaskFactory() {
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBSettingSaveTask()
		);
	}

}

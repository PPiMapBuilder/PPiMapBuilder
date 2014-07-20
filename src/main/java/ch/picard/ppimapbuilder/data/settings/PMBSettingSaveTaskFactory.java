package ch.picard.ppimapbuilder.data.settings;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class PMBSettingSaveTaskFactory extends AbstractTaskFactory {
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					PMBSettings.save();
				}
			}
		);
	}

}

package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PMBMenuFactory extends AbstractTaskFactory {

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PMBMenu());
	}

}

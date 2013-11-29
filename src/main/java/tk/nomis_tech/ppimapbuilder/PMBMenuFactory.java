package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBMenuFactory extends AbstractTaskFactory {

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PMBQueryMenu());
	}

}

package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBQueryMenuTaskFactory extends AbstractTaskFactory {

	private QueryWindow queryWindow;

	public PMBQueryMenuTaskFactory(QueryWindow queryWindow) {
		this.queryWindow = queryWindow;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBQueryMenuTask(queryWindow)
		);
	}

}

package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class TaskFactory extends AbstractTaskFactory {

	private final WebServiceHelper webServiceHelper;

	public TaskFactory(WebServiceHelper webServiceHelper) {
		this.webServiceHelper = webServiceHelper;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new Task(webServiceHelper));
	}
}
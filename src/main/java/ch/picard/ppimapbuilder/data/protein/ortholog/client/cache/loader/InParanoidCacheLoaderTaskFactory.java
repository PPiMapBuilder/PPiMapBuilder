package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import ch.picard.ppimapbuilder.data.organism.Organism;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

import java.util.List;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {
	private List<Organism> organisms;
	private Task callback;
	private String message;
	private String error;

	@Override
	public TaskIterator createTaskIterator() {
		message = null;
		error = null;
		return new TaskIterator(
				new InParanoidCacheLoaderTask(this, callback)
		);
	}

	public void setOrganisms(List<Organism> organisms) {
		this.organisms = organisms;
	}

	public List<Organism> getOrganisms() {
		return organisms;
	}

	public void setCallback(Task callback) {
		this.callback = callback;
	}

	public Task getCallback() {
		return callback;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}
}

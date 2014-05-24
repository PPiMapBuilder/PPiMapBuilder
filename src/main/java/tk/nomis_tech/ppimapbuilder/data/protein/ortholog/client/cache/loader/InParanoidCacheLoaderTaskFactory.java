package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.task.TaskListener;

import java.util.List;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {
	private final List<Organism> organisms;

	private final TaskListener listener;

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms) {
		this(organisms, null);
	}

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms, TaskListener listener) {
		this.organisms = organisms;
		this.listener = listener;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new InParanoidCacheLoaderTask(this));
	}

	public List<Organism> getOrganisms() {
		return organisms;
	}

	public TaskListener getListener() {
		return listener;
	}
}

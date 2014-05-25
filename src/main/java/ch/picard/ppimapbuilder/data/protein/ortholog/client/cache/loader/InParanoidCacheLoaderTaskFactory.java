package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.List;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {
	private final List<Organism> organisms;

	private final Runnable listener;

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms) {
		this(organisms, null);
	}

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms, Runnable listener) {
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

	public Runnable getListener() {
		return listener;
	}
}

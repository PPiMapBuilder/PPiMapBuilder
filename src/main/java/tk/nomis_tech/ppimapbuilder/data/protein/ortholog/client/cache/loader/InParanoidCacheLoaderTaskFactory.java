package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;

import java.util.List;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {

	private final List<Organism> organisms;

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms) {
		this.organisms = organisms;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new InParanoidCacheLoaderTask(organisms)
		);
	}
}

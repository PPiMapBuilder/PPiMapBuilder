package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import org.cytoscape.work.TaskMonitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

class FetchOrthologsOfInteractorsTask extends AbstractInteractionQueryTask {

	// Input
	private final List<Organism> otherOrganisms;
	private final UniProtEntrySet interactorPool;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;

	public FetchOrthologsOfInteractorsTask(
			ThreadedClientManager threadedClientManager,
			List<Organism> otherOrganisms, UniProtEntrySet interactorPool, Double minimum_orthology_score
	) {
		super(threadedClientManager);
		this.otherOrganisms = otherOrganisms;
		this.interactorPool = interactorPool;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.proteinOrthologClient = threadedClientManager.getOrCreateProteinOrthologClient();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch orthologs of interactors in other organisms...");

		//optimization : searching orthologs in a number of organisms limited to
		// the maximum organism in memory cache
		int step = PMBProteinOrthologCacheClient.MAX_NB_MEMORY_CACHE - 1;

		for (int i = 0; i < otherOrganisms.size(); i += step) {
			taskMonitor.setProgress(((double) i) / ((double) otherOrganisms.size()));

			final Map<Protein, Map<Organism, List<OrthologScoredProtein>>> orthologs =
					proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(
							interactorPool,
							otherOrganisms.subList(
									i,
									Math.min(
											otherOrganisms.size(),
											i + step
									)
							),
							MINIMUM_ORTHOLOGY_SCORE
					);

			interactorPool.addOrthologs(orthologs);

/*			for (UniProtEntry entry : interactorPool) {
				for (Organism organism : orthologs.get(entry).keySet())
					entry.addOrthologs(orthologs.get(entry).get(organism));
			}*/
		}

		threadedClientManager.unRegister(proteinOrthologClient);
	}

}

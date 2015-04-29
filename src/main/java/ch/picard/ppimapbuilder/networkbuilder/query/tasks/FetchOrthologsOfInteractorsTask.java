/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FetchOrthologsOfInteractorsTask extends AbstractInteractionQueryTask {

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

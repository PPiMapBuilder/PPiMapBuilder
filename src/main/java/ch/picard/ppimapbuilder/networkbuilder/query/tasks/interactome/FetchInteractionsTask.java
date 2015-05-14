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

package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.*;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.task.AbstractThreadedTask;
import ch.picard.ppimapbuilder.util.task.TaskMemoryMonitoringDaemon;
import com.google.common.collect.Iterators;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;

public class FetchInteractionsTask extends AbstractThreadedTask {

	private ConcurrentFetcherIterator<BinaryInteraction> interactionIterator = null;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final Collection<PsicquicService> psicquicServices;
	private final Organism referenceOrganism;

	public FetchInteractionsTask(
			ExecutorServiceManager executorServiceManager,
			UniProtEntrySet interactorPool, HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			Collection<PsicquicService> psicquicServices, Organism referenceOrganism) {
		super(executorServiceManager);
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
		this.psicquicServices = psicquicServices;
		this.referenceOrganism = referenceOrganism;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("PPiMapBuilder interactome network query");
		taskMonitor.setProgress(0);
		taskMonitor.setStatusMessage("Fetch PSICQUIC interactions...");

		// PSICQUIC Request builder
		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(psicquicServices)
				.addGetByTaxon(referenceOrganism.getTaxId());

		// Combined interaction filter
		final InteractionFilter filter = InteractionUtils.combineFilters(
				new ProgressMonitoringInteractionFilter(
						builder.getEstimatedInteractionsCount(),
						new ProgressTaskMonitor(taskMonitor)
				),

				// Iteractors only in specific organism
				new OrganismInteractorFilter(referenceOrganism),

				// UniProt only iteractors
				new UniProtInteractorFilter(),

				// Creating an empty UniProt entry
				new InteractorFilter() {

					@Override
					public boolean isValidInteractor(Interactor interactor) {
						final Protein interactorProtein = InteractionUtils.getProteinInteractor(interactor);

						if (interactorProtein != null) {
							final UniProtEntry entry = new UniProtEntry.Builder(interactorProtein).build();
							try {
								interactorPool.add(entry);
							} catch (NullPointerException e) {
								e.printStackTrace();
							}
							return true;
						}
						return false;
					}
				}
		);

		// Daemon thread monitoring the used memory
		final Thread memoryMonitoringDaemon = new TaskMemoryMonitoringDaemon(this);

		try {
			memoryMonitoringDaemon.start();

			// Fetch interaction iterator
			interactionIterator =
					new ConcurrentFetcherIterator<BinaryInteraction>(
							builder.getPsicquicRequests(),
							executorServiceManager//,
							//new ProgressTaskMonitor(taskMonitor)
					);

			// Interaction stream = Fetch interactions > Filter interaction > Cluster interactions
			interactionsByOrg.put(
					referenceOrganism,

					// Cluster interactions
					InteractionUtils.clusterInteraction(

							// Filter interactions
							Iterators.filter(

									// Fetch interactions
									interactionIterator,
									filter
							)

					)
			);
		} finally {
			memoryMonitoringDaemon.interrupt();
		}

	}

	@Override
	public void cancel() {
		if(interactionIterator != null)
			interactionIterator.cancel();
		super.cancel();
	}
}

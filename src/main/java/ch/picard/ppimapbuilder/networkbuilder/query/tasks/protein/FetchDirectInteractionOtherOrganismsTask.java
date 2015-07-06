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

package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.InteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.OrganismInteractorFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.ProgressMonitoringInteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.UniProtInteractorFilter;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.concurrent.IteratorRequest;
import ch.picard.ppimapbuilder.util.task.AbstractThreadedTask;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class FetchDirectInteractionOtherOrganismsTask extends AbstractThreadedTask {

	private final Collection<PsicquicService> psicquicServices;

	// Input
	private final List<Organism> otherOrganisms;
	private final Organism referenceOrganism;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final Set<UniProtEntry> proteinOfInterestPool;

	// Output
	private final UniProtEntrySet interactorPool;
	private final List<BinaryInteraction> directInteractions;

	public FetchDirectInteractionOtherOrganismsTask(
			ExecutorServiceManager executorServiceManager,
			Collection<PsicquicService> psicquicServices, List<Organism> otherOrganisms, Organism referenceOrganism,
			Double minimum_orthology_score, Set<UniProtEntry> proteinOfInterestPool,
			UniProtEntrySet interactorPool,
			List<BinaryInteraction> directInteractions
	) {
		super(executorServiceManager);
		this.psicquicServices = psicquicServices;
		this.otherOrganisms = otherOrganisms;
		this.referenceOrganism = referenceOrganism;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;
		this.directInteractions = directInteractions;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch direct interaction of input proteins orthologs in other organisms...");

		final List<IteratorRequest<BinaryInteraction>> requests = Lists.newArrayList();
		int estimatedInteractionCount = 0;
		for (Organism organism : otherOrganisms) {
			final PrimaryInteractionQuery primaryInteractionRequest = new PrimaryInteractionQuery(
					executorServiceManager, psicquicServices, referenceOrganism, organism,
					proteinOfInterestPool, interactorPool, MINIMUM_ORTHOLOGY_SCORE, null
			);
			requests.add(primaryInteractionRequest);
			estimatedInteractionCount += primaryInteractionRequest.getEstimatedInteractionCount();
		}

		InteractionFilter filter = InteractionUtils.combineFilters(
				new ProgressMonitoringInteractionFilter(estimatedInteractionCount, taskMonitor),
				new UniProtInteractorFilter(),
				new OrganismInteractorFilter(otherOrganisms)
		);

		Iterators.addAll(
				directInteractions,
				Iterators.filter(
						new ConcurrentFetcherIterator<BinaryInteraction>(
								requests,
								executorServiceManager
						),
						filter
				)
		);
		taskMonitor.setProgress(1.0);
	}

}

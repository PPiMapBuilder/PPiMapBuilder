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
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequest;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.InteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.InteractorFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.OrganismInteractorFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.ProgressMonitoringInteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.UniProtInteractorFilter;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.concurrent.IteratorRequest;
import com.google.common.collect.Iterators;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class PrimaryInteractionQuery implements IteratorRequest<BinaryInteraction> {

	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ExecutorServiceManager executorServiceManager;
	private final TaskMonitor taskMonitor;

	private final Organism referenceOrganism;
	private final Organism organism;
	private final boolean inReferenceOrgansim;
	private final UniProtEntrySet interactorPool;

	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	private List<PsicquicRequest> interactionRequests;
	private int estimatedInteractionCount = 0;

	public PrimaryInteractionQuery(
			ExecutorServiceManager executorServiceManager, Collection<PsicquicService> psicquicServices,
			Organism referenceOrganism, Organism organism,
			Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool,
			Double minimum_orthology_score,
			TaskMonitor taskMonitor
	) {
		this.referenceOrganism = referenceOrganism;
		this.organism = organism;
		this.inReferenceOrgansim = organism.equals(referenceOrganism);

		this.interactorPool = interactorPool;

		this.executorServiceManager = executorServiceManager;
		this.taskMonitor = taskMonitor;
		{
			final InParanoidClient inParanoidClient = new InParanoidClient();
			inParanoidClient.setCache(PMBProteinOrthologCacheClient.getInstance());

			this.proteinOrthologClient = new ThreadedProteinOrthologClientDecorator(
					new ProteinOrthologWebCachedClient(
							inParanoidClient,
							PMBProteinOrthologCacheClient.getInstance()
					),
					executorServiceManager
			);
		}
		this.uniProtClient = new UniProtEntryClient(executorServiceManager);

		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		// Get list of protein of interest's orthologs in this organism
		Set<String> proteinOfInterestInOrganism = interactorPool.identifiersInOrganism(proteinOfInterestPool, organism);

		// Prepare requests
		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(psicquicServices);
		for (String id : proteinOfInterestInOrganism) {
			builder.addGetByTaxonAndId(id, organism.getTaxId());
		}
		estimatedInteractionCount = builder.getEstimatedInteractionsCount();
		interactionRequests = builder.getRequests();
	}

	public Iterator<BinaryInteraction> call() throws Exception {
		if(taskMonitor != null) taskMonitor.setProgress(0);

		//Prepare interaction filters
		final InteractionFilter filter = InteractionUtils.combineFilters(
				taskMonitor != null ? new ProgressMonitoringInteractionFilter(
						estimatedInteractionCount,
						taskMonitor
				) : null,
				new UniProtInteractorFilter(),
				new OrganismInteractorFilter(organism),

				// Filter that rejects interactions with at least one interactor not found in the reference organism (even with orthology)
				// This filters also extracts the interactor UniProt entry to be stored in the newInteractors UniProtEntrySet
				new InteractorFilter() {
					@Override
					public boolean isValidInteractor(Interactor interactor) {
						final Protein interactorProtein = InteractionUtils.getProteinInteractor(interactor);

						Set<Protein> proteinsInReferenceOrganism = new HashSet<Protein>();
						if (inReferenceOrgansim)
							proteinsInReferenceOrganism.add(interactorProtein);
						else try {
							proteinsInReferenceOrganism.addAll(
									proteinOrthologClient.getOrtholog(
											interactorProtein, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE
									)
							);
						} catch (Exception ignored) {}

						boolean ok = false;
						for (Protein proteinInReferenceOrganism : proteinsInReferenceOrganism) {
							String uniProtId = proteinInReferenceOrganism.getUniProtId();

							// Find in existing protein pools
							UniProtEntry entry = null;
							synchronized (interactorPool) {
								entry = interactorPool.findByPrimaryAccession(uniProtId);
							}

							// Find on UniProt
							if (entry == null) {
								try {
									entry = uniProtClient.retrieveProteinData(uniProtId);

									if (entry != null) synchronized (interactorPool) {
										interactorPool.add(entry);
									}
								} catch (IOException ignored) {}
							}

							if (entry != null) {
								if (!inReferenceOrgansim) synchronized (interactorPool) {
									interactorPool.addOrtholog(entry, Collections.singletonList(interactorProtein));
								}
								ok = true;
							}
						}
						return ok;
					}
				}
		);

		return Iterators.filter(
				new ConcurrentFetcherIterator<BinaryInteraction>(
						interactionRequests,
						executorServiceManager
				),
				filter
		);
	}

	public int getEstimatedInteractionCount() {
		return estimatedInteractionCount;
	}
}

package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.*;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

class PrimaryInteractionQuery implements Callable<PrimaryInteractionQuery> {

	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ExecutorServiceManager executorServiceManager;
	private final TaskMonitor taskMonitor;

	private final Collection<PsicquicService> psicquicServices;
	private final Organism referenceOrganism;
	private final Organism organism;
	private final boolean inReferenceOrgansim;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final UniProtEntrySet interactorPool;

	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	// Ouput
	private final Collection<BinaryInteraction> newInteractions;

	public PrimaryInteractionQuery(
			ExecutorServiceManager executorServiceManager, Collection<PsicquicService> psicquicServices, Organism referenceOrganism, Organism organism, Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool,
			Double minimum_orthology_score,
			TaskMonitor taskMonitor
	) {
		this.psicquicServices = psicquicServices;
		this.referenceOrganism = referenceOrganism;
		this.organism = organism;
		this.inReferenceOrgansim = organism.equals(referenceOrganism);

		this.proteinOfInterestPool = proteinOfInterestPool;
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

		this.newInteractions = new ArrayList<BinaryInteraction>();
	}

	public PrimaryInteractionQuery call() throws Exception {
		taskMonitor.setProgress(0);

		// Get list of protein of interest's orthologs in this organism
		Set<String> proteinOfInterestInOrganism = interactorPool.identifiersInOrganism(proteinOfInterestPool, organism);

		// Construct Psicquic requests
		PsicquicRequestBuilder requestBuilder = new PsicquicRequestBuilder(psicquicServices);
		for (String uniprotId : proteinOfInterestInOrganism) {
			requestBuilder.addGetByTaxonAndId(uniprotId, organism.getTaxId());
		}

		// Run requests concurrently
		final Iterator<BinaryInteraction> interactionIterator = new ConcurrentFetcherIterator<BinaryInteraction>(
				requestBuilder.getPsicquicRequests(),
				executorServiceManager
		);

		final InteractionFilter filter = InteractionUtils.combineFilters(
				new ProgressMonitoringInteractionFilter(
						requestBuilder.getEstimatedInteractionsCount(),
						new ProgressTaskMonitor(taskMonitor)
				),

				//uniprot protein only
				new UniProtInteractorFilter(),

				//specified organism only
				new OrganismInteractorFilter(organism),

				//fetch uniprot entries
				new UniProtFetcherInteractionFilter(
						proteinOrthologClient, inReferenceOrgansim, referenceOrganism,
						MINIMUM_ORTHOLOGY_SCORE, interactorPool, uniProtClient
				)
		);

		// Filter interaction and extract interactors in the reference organism
		newInteractions.addAll(
				Lists.newArrayList(
						Iterators.filter(
								interactionIterator,
								filter
						)
				)
		);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public Collection<BinaryInteraction> getNewInteractions() {
		return newInteractions;
	}
}

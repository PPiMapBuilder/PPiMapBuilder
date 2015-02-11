package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.UniProtFetcherInteractionFilter;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.*;
import java.util.concurrent.Callable;

class PrimaryInteractionQuery implements Callable<PrimaryInteractionQuery> {

	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ThreadedClientManager threadedClientManager;
	private final TaskMonitor taskMonitor;

	private final Organism referenceOrganism;
	private final Organism organism;
	private final boolean inReferenceOrgansim;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final UniProtEntrySet interactorPool;

	private final ThreadedPsicquicClient psicquicClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	// Ouput
	private final Collection<BinaryInteraction> newInteractions;

	public PrimaryInteractionQuery(
			Organism referenceOrganism, Organism organism, Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool,
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score,
			TaskMonitor taskMonitor
	) {
		this.referenceOrganism = referenceOrganism;
		this.organism = organism;
		this.inReferenceOrgansim = organism.equals(referenceOrganism);

		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;

		this.threadedClientManager = threadedClientManager;
		this.taskMonitor = taskMonitor;
		this.psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
		this.proteinOrthologClient = threadedClientManager.getOrCreateProteinOrthologClient();
		this.uniProtClient = threadedClientManager.getOrCreateUniProtClient();

		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.newInteractions = new ArrayList<BinaryInteraction>();
	}

	public PrimaryInteractionQuery call() throws Exception {
		taskMonitor.setProgress(0);

		// Get list of protein of interest's orthologs in this organism
		Set<String> proteinOfInterestInOrganism = interactorPool.identifiersInOrganism(proteinOfInterestPool, organism);

		// Prepare MiQL queries
		ArrayList<String> queries = new ArrayList<String>();
		for (String id : proteinOfInterestInOrganism) {
			queries.add(
					InteractionUtils.generateMiQLQueryIDTaxID(
							id,
							organism.getTaxId()
					)
			);
		}

		// Fetch primary interactions
		List<BinaryInteraction> interactions = psicquicClient.getByQueries(queries);
		threadedClientManager.unRegister(psicquicClient);

		// Filter interaction and extract interactors in the reference organism
		final Double[] i = new Double[]{0d, 0d};
		final double size = interactions.size();
		newInteractions.addAll(InteractionUtils.filterConcurrently(
				threadedClientManager.getExecutorServiceManager(),
				interactions,
				new ProgressTaskMonitor(taskMonitor),

				new InteractionUtils.UniProtInteractionFilter(),
				new InteractionUtils.OrganismInteractionFilter(organism),

				new UniProtFetcherInteractionFilter(
						proteinOrthologClient, inReferenceOrgansim, referenceOrganism,
						MINIMUM_ORTHOLOGY_SCORE, interactorPool, uniProtClient
				)
		));

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtClient);

		if (i[1] < 1.0) taskMonitor.setProgress(1.0);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public Collection<BinaryInteraction> getNewInteractions() {
		return newInteractions;
	}
}

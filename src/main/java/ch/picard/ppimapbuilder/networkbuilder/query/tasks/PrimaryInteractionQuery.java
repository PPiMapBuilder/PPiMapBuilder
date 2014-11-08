package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntryCollection;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

class PrimaryInteractionQuery implements Callable<PrimaryInteractionQuery> {

	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ThreadedClientManager threadedClientManager;
	private final TaskMonitor taskMonitor;

	private final Organism referenceOrganism;
	private final Organism organism;
	private final boolean inReferenceOrgansim;
	private final UniProtEntrySet proteinOfInterestPool;
	private final UniProtEntrySet interactorPool;

	private final ThreadedPsicquicClient psicquicClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	// Ouput
	private final UniProtEntryCollection newInteractors;
	private final Collection<BinaryInteraction> newInteractions;

	public PrimaryInteractionQuery(
			Organism referenceOrganism, Organism organism, UniProtEntrySet proteinOfInterestPool, UniProtEntrySet interactorPool,
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

		this.newInteractors = new UniProtEntryCollection(referenceOrganism);
		this.newInteractions = new ArrayList<BinaryInteraction>();
	}

	public PrimaryInteractionQuery call() throws Exception {
		// Get list of protein of interest's orthologs in this organism
		Set<String> proteinOfInterestInOrganism = proteinOfInterestPool.identifiersInOrganism(organism).keySet();

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

				new InteractionUtils.UniProtInteractionFilter(),
				new InteractionUtils.OrganismInteractionFilter(organism),

				// Filter that rejects interactions with at least one interactor not found in the reference organism (even with orthology)
				// This filters also extracts the interactor UniProt entry to be stored in the newInteractors UniProtEntrySet
				new InteractionUtils.InteractorFilter() {
					@Override
					public boolean isValidInteractor(Interactor interactor) {
						final Protein interactorProtein = InteractionUtils.getProteinInteractor(interactor);

						Protein proteinInReferenceOrganism = null;
						if (inReferenceOrgansim)
							proteinInReferenceOrganism = interactorProtein;
						else try {
							proteinInReferenceOrganism = proteinOrthologClient.getOrtholog(interactorProtein, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
						} catch (Exception ignored) {}

						if (proteinInReferenceOrganism != null) {
							String uniProtId = proteinInReferenceOrganism.getUniProtId();

							// Find in existing protein pools
							UniProtEntry entry = proteinOfInterestPool.find(uniProtId);
							if (entry == null) synchronized (interactorPool) {
								entry = interactorPool.find(uniProtId);
							}
							if (entry == null) synchronized (newInteractors) {
								entry = newInteractors.find(uniProtId);
							}
							if (entry != null) {
								if (!inReferenceOrgansim)
									entry.addOrtholog(interactorProtein);
								return true;
							}

							// Find on UniProt
							try {
								entry = uniProtClient.retrieveProteinData(uniProtId);
							} catch (IOException ignored) {}

							if (entry != null) {
								if (!inReferenceOrgansim)
									entry.addOrtholog(interactorProtein);
								synchronized (newInteractors) {
									newInteractors.add(entry);
								}
								return true;
							}
						}
						return false;
					}

					@Override
					public boolean isValidInteraction(BinaryInteraction interaction) {
						synchronized (i[0]) {
							double percent = Math.floor((++i[0] / size) * 100) / 100;
							if (percent > i[1])
								taskMonitor.setProgress(i[1] = percent);
						}
						return super.isValidInteraction(interaction);
					}
				}
		));

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtClient);

		if(i[1] < 1.0) taskMonitor.setProgress(1.0);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public Collection<UniProtEntry> getNewInteractors() {
		return newInteractors;
	}

	public Collection<BinaryInteraction> getNewInteractions() {
		return newInteractions;
	}
}

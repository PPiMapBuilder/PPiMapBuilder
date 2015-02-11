package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologClient;
import psidev.psi.mi.tab.model.Interactor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Filter that rejects interactions with at least one interactor not found in the reference organism (even with orthology).
 * This filters also extracts the interactor UniProt entry to be stored in the newInteractors UniProtEntrySet
 */
public class UniProtFetcherInteractionFilter extends InteractionUtils.InteractorFilter {
	private final ProteinOrthologClient proteinOrthologClient;
	private final boolean inReferenceOrgansim;
	private final Organism referenceOrganism;
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final UniProtEntrySet interactorPool;
	private final UniProtEntryClient uniProtClient;

	public UniProtFetcherInteractionFilter(
			ProteinOrthologClient proteinOrthologClient, boolean inReferenceOrgansim, Organism referenceOrganism,
			Double minimum_orthology_score, UniProtEntrySet interactorPool, UniProtEntryClient uniProtClient
	) {
		this.proteinOrthologClient = proteinOrthologClient;
		this.inReferenceOrgansim = inReferenceOrgansim;
		this.referenceOrganism = referenceOrganism;
		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.interactorPool = interactorPool;
		this.uniProtClient = uniProtClient;
	}

	@Override
	public boolean isValidInteractor(Interactor interactor) {
		final Protein interactorProtein = InteractionUtils.getProteinInteractor(interactor);

		Set<Protein> proteinsInReferenceOrganism = new HashSet<Protein>();
		if (inReferenceOrgansim)
			proteinsInReferenceOrganism.add(interactorProtein);
		else try {
			proteinsInReferenceOrganism.addAll(proteinOrthologClient.getOrtholog(interactorProtein, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE));
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
				} catch (IOException ignored) {
				}
			}

			if (entry != null) {
				if (!inReferenceOrgansim) synchronized (interactorPool) {
					interactorPool.addOrtholog(entry, Arrays.asList(interactorProtein));
				}
				ok = true;
			}
		}
		return ok;
	}
}
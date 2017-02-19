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

package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
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
public class UniProtFetcherInteractionFilter extends InteractorFilter {
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
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
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.OrganismInteractorFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.ProgressMonitoringInteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.UniProtInteractorFilter;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.task.AbstractThreadedTask;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.Binary2Encore;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class FetchInteractionsTask extends AbstractThreadedTask {

	private final Collection<PsicquicService> psicquicServices;

	// Input
	private final List<Organism> allOrganisms;
	private final UniProtEntrySet interactorPool;
	private final Set<UniProtEntry> proteinOfInterestPool;

	private final List<BinaryInteraction> directInteractions;
	// Output
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	public FetchInteractionsTask(
			ExecutorServiceManager webServiceClientFactory,
			Collection<PsicquicService> psicquicServices, List<Organism> allOrganisms, UniProtEntrySet interactorPool, Set<UniProtEntry> proteinOfInterestPool,
			List<BinaryInteraction> directInteractions,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		super(webServiceClientFactory);
		this.psicquicServices = psicquicServices;
		this.allOrganisms = allOrganisms;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.directInteractions = directInteractions;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch secondary interactions in all organisms...");

		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(psicquicServices);
		for (Organism organism : allOrganisms) {
			final Set<String> proteins = interactorPool.identifiersInOrganism(organism).keySet();
			final Set<String> proteinsOfInterest = interactorPool.identifiersInOrganism(proteinOfInterestPool, organism);
			proteins.removeAll(proteinsOfInterest);
			builder.addGetByProteinPool(proteins, organism.getTaxId());
		}
		final int estimatedCount = builder.getEstimatedInteractionsCount();
		final List<PsicquicRequest> requests = builder.getRequests();

		final InteractionFilter filter = InteractionUtils.combineFilters(
				new ProgressMonitoringInteractionFilter(estimatedCount, taskMonitor),
				new UniProtInteractorFilter(),
				new OrganismInteractorFilter(allOrganisms)
		);

		final Iterator<BinaryInteraction> interactions = Iterators.concat(
				directInteractions.iterator(),
				Iterators.filter(
						new ConcurrentFetcherIterator<BinaryInteraction>(
								requests,
								executorServiceManager
						),
						filter
				)
		);
		/*final Collection<EncoreInteraction> encoreInteractions = InteractionUtils.clusterInteraction(
				interactions1
		);
		for (EncoreInteraction interaction : interactions) {
			Organism organismA = getOrganism(interaction.getOrganismsA());
			Organism organismB = getOrganism(interaction.getOrganismsB());
			if (organismA == organismB && organismA != null) {
				Collection<EncoreInteraction> interactions = interactionsByOrg.get(organismA);
				if (interactions == null) {
					interactions = Lists.newArrayList();
					interactionsByOrg.put(organismA, interactions);
				}
				interactions.add(interaction);
			} else {
				System.err.println("Unrecognized organisms");
				System.err.println("A: " + organismA + " " + interaction.getOrganismsA());
				System.err.println("B: " + organismB + " " + interaction.getOrganismsB());
			}
		}
		*/

		final Binary2Encore binary2Encore = new Binary2Encore();

		while (interactions.hasNext()) {
			final BinaryInteraction interaction = interactions.next();
			final List<CrossReference> orgAIdentifiers = interaction.getInteractorA().getOrganism().getIdentifiers();
			final List<CrossReference> orgBIdentifiers = interaction.getInteractorB().getOrganism().getIdentifiers();
			Organism organismA = getOrganism(orgAIdentifiers);
			Organism organismB = getOrganism(orgBIdentifiers);
			if (organismA == organismB && organismA != null) {
				Collection<EncoreInteraction> encoreInteractions = interactionsByOrg.get(organismA);
				if (encoreInteractions == null) {
					encoreInteractions = Lists.newArrayList();
					interactionsByOrg.put(organismA, encoreInteractions);
				}
				encoreInteractions.add(binary2Encore.getEncoreInteraction(interaction));
			} else {
				System.err.println("Unrecognized organisms");
				System.err.println("A: " + organismA + " " + orgAIdentifiers);
				System.err.println("B: " + organismB + " " + orgBIdentifiers);
			}
		}
	}

	private static Organism getOrganism(List<CrossReference> crossReferences) {
		for (CrossReference crossReference : crossReferences) {
			final Organism organism = OrganismUtils.findOrganismInMITABTaxId(
					InParanoidOrganismRepository.getInstance(),
					crossReference.getIdentifier()
			);
			if (organism != null) return organism;
		}
		return null;
	}

}

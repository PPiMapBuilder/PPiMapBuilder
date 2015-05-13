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

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;

public class ProteinNetworkQueryTaskFactory implements TaskFactory {

	private final ExecutorServiceManager executorServiceManager;
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;
	private final List<Organism> otherOrganisms;
	private final List<Organism> allOrganisms;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final Collection<PsicquicService> psicquicServices;

	public ProteinNetworkQueryTaskFactory(
			ExecutorServiceManager executorServiceManager, Collection<PsicquicService> psicquicServices,
			Double minimum_orthology_score, Collection<String> inputProteinIDs,
			Organism referenceOrganism, Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool,
			Collection<Organism> otherOrganisms, HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		this.executorServiceManager = executorServiceManager;
		this.psicquicServices = psicquicServices;
		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.inputProteinIDs = new ArrayList<String>(inputProteinIDs);
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;

		this.referenceOrganism = referenceOrganism;

		this.otherOrganisms = new ArrayList<Organism>(otherOrganisms);
		this.otherOrganisms.remove(referenceOrganism);

		this.allOrganisms = new ArrayList<Organism>();
		allOrganisms.addAll(this.otherOrganisms);
		allOrganisms.add(referenceOrganism);

		this.directInteractionsByOrg = new HashMap<Organism, Collection<BinaryInteraction>>();
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new PrepareProteinOfInterestTask(
						executorServiceManager,
						MINIMUM_ORTHOLOGY_SCORE, inputProteinIDs, referenceOrganism,
						proteinOfInterestPool, interactorPool
				),
				new FetchDirectInteractionReferenceOrganismTask(
						executorServiceManager, psicquicServices,
						referenceOrganism, proteinOfInterestPool, MINIMUM_ORTHOLOGY_SCORE,
						interactorPool, directInteractionsByOrg
				),
				new FetchOrthologsOfInteractorsTask(
						executorServiceManager,
						otherOrganisms, interactorPool, MINIMUM_ORTHOLOGY_SCORE
				),
				new FetchDirectInteractionOtherOrganismsTask(
						executorServiceManager, psicquicServices,
						otherOrganisms, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE, proteinOfInterestPool,
						interactorPool, directInteractionsByOrg
				),
				new FetchInteractionsTask(
						executorServiceManager, psicquicServices,
						allOrganisms, interactorPool, proteinOfInterestPool, directInteractionsByOrg,
						interactionsByOrg
				)
		);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}

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
    
package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.*;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;

public class PMBInteractionQueryTaskFactory implements TaskFactory {

	// Data input
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final List<Organism> otherOrganisms;
	private final List<Organism> allOrganisms;

	// Data output
	private final Set<UniProtEntry> proteinOfInterestPool; // not the same as user input
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

	// Temporary data
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;

	// Thread list
	private final ThreadedClientManager threadedClientManager;

	// Option
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	public PMBInteractionQueryTaskFactory(
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			UniProtEntrySet interactorPool,
			Set<UniProtEntry> proteinOfInterestPool,
			NetworkQueryParameters networkQueryParameters
	) {
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		// Retrieve user input
		referenceOrganism = networkQueryParameters.getReferenceOrganism();
		inputProteinIDs = new ArrayList<String>(new HashSet<String>(networkQueryParameters.getProteinOfInterestUniprotId()));
		otherOrganisms = networkQueryParameters.getOtherOrganisms();
		otherOrganisms.remove(referenceOrganism);
		allOrganisms = new ArrayList<Organism>();
		allOrganisms.addAll(otherOrganisms);
		allOrganisms.add(referenceOrganism);
		List<PsicquicService> selectedDatabases = networkQueryParameters.getSelectedDatabases();

		directInteractionsByOrg = new HashMap<Organism, Collection<BinaryInteraction>>();

		// Store thread pool used by web client and this task
		this.threadedClientManager = new ThreadedClientManager(
                new ExecutorServiceManager((Math.min(2, Runtime.getRuntime().availableProcessors()) + 1)*2),
				selectedDatabases
		);

		MINIMUM_ORTHOLOGY_SCORE = 0.85;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new PrepareProteinOfInterestTask(
						threadedClientManager,
						MINIMUM_ORTHOLOGY_SCORE, inputProteinIDs, referenceOrganism,
						proteinOfInterestPool, interactorPool
				),
				new FetchDirectInteractionReferenceOrganismTask(
						threadedClientManager,
						referenceOrganism, proteinOfInterestPool, MINIMUM_ORTHOLOGY_SCORE,
						interactorPool, directInteractionsByOrg
				),
				new FetchOrthologsOfInteractorsTask(
						threadedClientManager,
						otherOrganisms, interactorPool, MINIMUM_ORTHOLOGY_SCORE
				),
				new FetchDirectInteractionOtherOrganismsTask(
						threadedClientManager,
						otherOrganisms, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE, proteinOfInterestPool,
						interactorPool, directInteractionsByOrg
				),
				new FetchInteractionsTask(
						threadedClientManager,
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

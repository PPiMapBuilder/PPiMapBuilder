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

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome.InteractomeNetworkQueryTaskFactory;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein.ProteinNetworkQueryTaskFactory;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PMBInteractionQueryTaskFactory implements TaskFactory {

	private final ExecutorServiceManager executorServiceManager;

	// Data input
	private final Organism referenceOrganism;
	private final boolean interactomeQuery;
	// Data output
	private final Set<UniProtEntry> proteinOfInterestPool; // not the same as user input
	private final NetworkQueryParameters networkQueryParameters;

	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	private final UniProtEntrySet interactorPool;
	// Option
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	public PMBInteractionQueryTaskFactory(
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			UniProtEntrySet interactorPool,
			Set<UniProtEntry> proteinOfInterestPool,
			NetworkQueryParameters networkQueryParameters,
			ExecutorServiceManager executorServiceManager) {
		this.executorServiceManager = executorServiceManager;
		this.networkQueryParameters = networkQueryParameters;

		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		this.interactomeQuery = networkQueryParameters.isInteractomeQuery();

		this.referenceOrganism = networkQueryParameters.getReferenceOrganism();

		MINIMUM_ORTHOLOGY_SCORE = 0.85;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return (
				interactomeQuery ?
						new InteractomeNetworkQueryTaskFactory(
								executorServiceManager, networkQueryParameters.getSelectedDatabases(),
								referenceOrganism, interactorPool, interactionsByOrg
						) :
						new ProteinNetworkQueryTaskFactory(
								executorServiceManager, networkQueryParameters.getSelectedDatabases(),
								MINIMUM_ORTHOLOGY_SCORE,
								networkQueryParameters.getProteinOfInterestUniprotId(), referenceOrganism,
								proteinOfInterestPool, interactorPool, networkQueryParameters.getOtherOrganisms(),
								interactionsByOrg
						)
		).createTaskIterator();
	}

	@Override
	public boolean isReady() {
		return true;
	}

}

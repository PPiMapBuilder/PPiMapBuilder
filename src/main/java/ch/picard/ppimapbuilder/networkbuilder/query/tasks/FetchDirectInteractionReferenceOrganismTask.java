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
    
package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class FetchDirectInteractionReferenceOrganismTask extends AbstractInteractionQueryTask {

	// Input
	private final Organism referenceOrganism;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	// Output
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;

	public FetchDirectInteractionReferenceOrganismTask(
			ThreadedClientManager threadedClientManager,
			Organism referenceOrganism, Set<UniProtEntry> proteinOfInterestPool, Double minimum_orthology_score,
			UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg) {
		super(threadedClientManager);

		this.referenceOrganism = referenceOrganism;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.interactorPool = interactorPool;
		this.directInteractionsByOrg = directInteractionsByOrg;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch direct interactions of input proteins in reference organism...");
		final PrimaryInteractionQuery query = new PrimaryInteractionQuery(
				referenceOrganism, referenceOrganism, proteinOfInterestPool, interactorPool,
				threadedClientManager, MINIMUM_ORTHOLOGY_SCORE,
				taskMonitor
		).call();
		directInteractionsByOrg.put(referenceOrganism, query.getNewInteractions());
	}

}

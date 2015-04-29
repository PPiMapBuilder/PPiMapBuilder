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
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PrepareProteinOfInterestTask extends AbstractInteractionQueryTask {

	// Intput
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;

	// Output
	private final Set<UniProtEntry> proteinOfInterestPool; // not the same as user input
	private final UniProtEntrySet interactorPool;

	private final UniProtEntryClient uniProtEntryClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;

	public PrepareProteinOfInterestTask(
			ThreadedClientManager threadedClientManager,
			Double minimum_orthology_score, List<String> inputProteinIDs, Organism referenceOrganism,
			Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool
	) {
		super(threadedClientManager);

		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.inputProteinIDs = inputProteinIDs;
		this.referenceOrganism = referenceOrganism;

		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;

		uniProtEntryClient = threadedClientManager.getOrCreateUniProtClient();
		proteinOrthologClient = threadedClientManager.getOrCreateProteinOrthologClient();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("PPiMapBuilder interaction query");
		taskMonitor.setProgress(0d);

		taskMonitor.setStatusMessage("Fetch UniProt data for input proteins...");

		HashMap<String, UniProtEntry> uniProtEntries = uniProtEntryClient.retrieveProteinsData(inputProteinIDs);
		for (double i = 0, size = inputProteinIDs.size(); i < size; taskMonitor.setProgress(++i / size)) {
			String proteinID = inputProteinIDs.get((int) i);
			UniProtEntry entry = uniProtEntries.get(proteinID);

			if (entry != null) {
				entry = ProteinUtils.correctEntryOrganism(entry, referenceOrganism);

				if (!entry.getOrganism().equals(referenceOrganism)) {
					List<? extends Protein> orthologs = proteinOrthologClient.getOrtholog(entry, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
					if (!orthologs.isEmpty()) {
						for (Protein ortholog : orthologs) {
							entry = uniProtEntryClient.retrieveProteinData(ortholog.getUniProtId());
							interactorPool.addOrtholog(entry, Arrays.asList(ortholog));
						}
					} else entry = null;
				}
			}

			if (entry == null) {
				System.err.println(proteinID + " was not found on UniProt in the reference organism.");
				//TODO : warn the user
				continue;
			}

			// Save the protein into the interactor pool and protein of interest pool
			proteinOfInterestPool.add(entry);
		}

		interactorPool.addAll(proteinOfInterestPool);

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtEntryClient);
	}

}

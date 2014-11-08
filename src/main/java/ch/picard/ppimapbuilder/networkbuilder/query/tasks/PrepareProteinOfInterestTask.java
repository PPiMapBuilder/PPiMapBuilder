package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;

import java.util.HashMap;
import java.util.List;

public class PrepareProteinOfInterestTask extends AbstractInteractionQueryTask {

	// Intput
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;

	// Output
	private final UniProtEntrySet proteinOfInterestPool; // not the same as user input
	private final UniProtEntrySet interactorPool;

	private final UniProtEntryClient uniProtEntryClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;

	public PrepareProteinOfInterestTask(
			ThreadedClientManager threadedClientManager,
			Double minimum_orthology_score, List<String> inputProteinIDs, Organism referenceOrganism,
			UniProtEntrySet proteinOfInterestPool, UniProtEntrySet interactorPool
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
		for (int i = 0, inputProteinIDsSize = inputProteinIDs.size(); i < inputProteinIDsSize; i++) {
			taskMonitor.setProgress(((double)i)/((double)inputProteinIDsSize));

			String proteinID = inputProteinIDs.get(i);
			UniProtEntry entry = uniProtEntries.get(proteinID);

			if (entry != null) {
				entry = ProteinUtils.correctEntryOrganism(entry, referenceOrganism);

				if (!entry.getOrganism().equals(referenceOrganism)) {
					Protein ortholog = proteinOrthologClient.getOrtholog(entry, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
                    if(ortholog != null) {
	                    entry = uniProtEntryClient.retrieveProteinData(ortholog.getUniProtId());
	                    entry.addOrtholog(ortholog);
                    }
                    else entry = null;
				}
			}

			if (entry == null) {
				System.err.println(proteinID + " was not found on UniProt in the reference organism.");
				//TODO : warn the user
				continue;
			}

			// Save the protein into the interactor pool
			if(proteinOfInterestPool.add(entry)) {
				interactorPool.add(entry);
			}
		}

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtEntryClient);
	}

}

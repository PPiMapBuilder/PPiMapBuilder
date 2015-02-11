package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class PrepareProteinOfInterestTask extends AbstractInteractionQueryTask {

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

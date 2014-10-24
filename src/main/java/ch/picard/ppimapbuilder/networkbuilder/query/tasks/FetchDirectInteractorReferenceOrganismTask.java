package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;

public class FetchDirectInteractorReferenceOrganismTask extends AbstractInteractionQueryTask {

	// Input
	private final Organism referenceOrganism;
	private final UniProtEntrySet proteinOfInterestPool;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	// Output
	private final UniProtEntrySet interactorPool;

	public FetchDirectInteractorReferenceOrganismTask(
			ThreadedClientManager threadedClientManager,
			Organism referenceOrganism, UniProtEntrySet proteinOfInterestPool, Double minimum_orthology_score,
			UniProtEntrySet interactorPool
	) {
		super(threadedClientManager);

		this.referenceOrganism = referenceOrganism;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.interactorPool = interactorPool;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch interactors of input proteins in reference organism...");
		interactorPool.addAll(
			new PrimaryInteractionQuery(
					referenceOrganism, referenceOrganism, proteinOfInterestPool, interactorPool,
					threadedClientManager, MINIMUM_ORTHOLOGY_SCORE,
					taskMonitor
			).call().getNewInteractors()
		);
	}

}

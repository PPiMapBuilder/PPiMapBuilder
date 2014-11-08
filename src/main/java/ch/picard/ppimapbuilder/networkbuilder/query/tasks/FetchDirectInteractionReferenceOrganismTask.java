package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.HashMap;

public class FetchDirectInteractionReferenceOrganismTask extends AbstractInteractionQueryTask {

	// Input
	private final Organism referenceOrganism;
	private final UniProtEntrySet proteinOfInterestPool;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	// Output
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;

	public FetchDirectInteractionReferenceOrganismTask(
			ThreadedClientManager threadedClientManager,
			Organism referenceOrganism, UniProtEntrySet proteinOfInterestPool, Double minimum_orthology_score,
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
		interactorPool.addAll(query.getNewInteractors());
		directInteractionsByOrg.put(referenceOrganism, query.getNewInteractions());
	}

}

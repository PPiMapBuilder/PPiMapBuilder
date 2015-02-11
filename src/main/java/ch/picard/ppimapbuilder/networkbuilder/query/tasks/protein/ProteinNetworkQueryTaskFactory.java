package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;

public class ProteinNetworkQueryTaskFactory implements TaskFactory {

	private final ThreadedClientManager threadedClientManager;
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;
	private final List<Organism> otherOrganisms;
	private final List<Organism> allOrganisms;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	public ProteinNetworkQueryTaskFactory(
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score, Collection<String> inputProteinIDs,
			Organism referenceOrganism, Set<UniProtEntry> proteinOfInterestPool, UniProtEntrySet interactorPool,
			Collection<Organism> otherOrganisms,  HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		this.threadedClientManager = threadedClientManager;
		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.inputProteinIDs = new ArrayList<String>(inputProteinIDs);
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;

		this.referenceOrganism = referenceOrganism;

		this.otherOrganisms = new ArrayList<Organism>(otherOrganisms);
		this.otherOrganisms.remove(referenceOrganism);

		this.allOrganisms = new ArrayList<Organism>();
		allOrganisms.addAll(otherOrganisms);
		allOrganisms.add(referenceOrganism);

		this.directInteractionsByOrg = new HashMap<Organism, Collection<BinaryInteraction>>();
		this.interactionsByOrg = interactionsByOrg;
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

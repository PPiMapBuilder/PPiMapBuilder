package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class InteractomeNetworkQueryTaskFactory implements TaskFactory {
	private final ThreadedClientManager threadedClientManager;
	private final Organism referenceOrganism;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	public InteractomeNetworkQueryTaskFactory(
			ThreadedClientManager threadedClientManager,
			Organism referenceOrganism, UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		this.threadedClientManager = threadedClientManager;
		this.referenceOrganism = referenceOrganism;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public TaskIterator createTaskIterator() {
		List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
		return new TaskIterator(
				new FetchInteractomeInteractionsTask(
						threadedClientManager,
						referenceOrganism,
						interactions
				),
				new FilterInteractomeInteractionsTask(
						threadedClientManager,
						referenceOrganism, interactions,
						interactorPool, interactionsByOrg
				)
		);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}

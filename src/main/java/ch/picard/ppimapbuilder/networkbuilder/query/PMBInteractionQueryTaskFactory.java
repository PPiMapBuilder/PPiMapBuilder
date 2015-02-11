package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome.InteractomeNetworkQueryTaskFactory;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein.ProteinNetworkQueryTaskFactory;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PMBInteractionQueryTaskFactory implements TaskFactory {

	// Data input
	private final Organism referenceOrganism;
	private final boolean interactomeQuery;

	// Data output
	private final Set<UniProtEntry> proteinOfInterestPool; // not the same as user input
	private final NetworkQueryParameters networkQueryParameters;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

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
		this.networkQueryParameters = networkQueryParameters;

		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		this.interactomeQuery = networkQueryParameters.isInteractomeQuery();

		// Retrieve user input
		referenceOrganism = networkQueryParameters.getReferenceOrganism();

		// Store thread pool used by web client and this task
		this.threadedClientManager = new ThreadedClientManager(
				new ExecutorServiceManager((Runtime.getRuntime().availableProcessors() + 1) * 2),
				networkQueryParameters.getSelectedDatabases()
		);

		MINIMUM_ORTHOLOGY_SCORE = 0.85;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return (
				interactomeQuery ?
						new InteractomeNetworkQueryTaskFactory(
								threadedClientManager,
								referenceOrganism, interactorPool, interactionsByOrg
						) :
						new ProteinNetworkQueryTaskFactory(
								threadedClientManager, MINIMUM_ORTHOLOGY_SCORE,
								networkQueryParameters.getProteinOfInterestUniprotId(), referenceOrganism,
								proteinOfInterestPool, interactorPool, networkQueryParameters.getOtherOrganisms(), interactionsByOrg
						)
		).createTaskIterator();
	}

	@Override
	public boolean isReady() {
		return true;
	}

}

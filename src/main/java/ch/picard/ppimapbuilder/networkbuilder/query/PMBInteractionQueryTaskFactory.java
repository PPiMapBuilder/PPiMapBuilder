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

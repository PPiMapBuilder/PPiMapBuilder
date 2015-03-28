package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

class FetchDirectInteractionReferenceOrganismTask extends AbstractInteractionQueryTask {

	// Input
	private final Organism referenceOrganism;
	private final Set<UniProtEntry> proteinOfInterestPool;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	// Output
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;
	private final Collection<PsicquicService> psicquicServices;

	public FetchDirectInteractionReferenceOrganismTask(
			ExecutorServiceManager webServiceClientFactory,
			Collection<PsicquicService> psicquicServices,
			Organism referenceOrganism, Set<UniProtEntry> proteinOfInterestPool, Double minimum_orthology_score,
			UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg) {
		super(webServiceClientFactory);
		this.psicquicServices = psicquicServices;

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
				executorServiceManager, psicquicServices, referenceOrganism, referenceOrganism, proteinOfInterestPool, interactorPool,
				MINIMUM_ORTHOLOGY_SCORE,
				taskMonitor
		).call();
		directInteractionsByOrg.put(referenceOrganism, query.getNewInteractions());
	}

}

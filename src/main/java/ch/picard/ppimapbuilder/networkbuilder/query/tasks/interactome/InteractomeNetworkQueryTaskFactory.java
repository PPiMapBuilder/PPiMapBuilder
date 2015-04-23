package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;

public class InteractomeNetworkQueryTaskFactory implements TaskFactory {
	private final ExecutorServiceManager executorServiceManager;
	private final Organism referenceOrganism;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final Collection<PsicquicService> psicquicServices;

	public InteractomeNetworkQueryTaskFactory(
			ExecutorServiceManager executorServiceManager,
			Collection<PsicquicService> psicquicServices, Organism referenceOrganism, UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		this.executorServiceManager = executorServiceManager;
		this.psicquicServices = psicquicServices;
		this.referenceOrganism = referenceOrganism;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new FetchInteractionsTask(
						executorServiceManager,
						interactorPool, interactionsByOrg, psicquicServices,
						referenceOrganism
				)
		);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}

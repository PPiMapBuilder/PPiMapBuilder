package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentListExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.List;

class FetchInteractomeInteractionsTask extends AbstractInteractionQueryTask {

	private final Organism referenceOrganism;
	private final List<BinaryInteraction> interactions;
	private final Collection<PsicquicService> psicquicServices;

	public FetchInteractomeInteractionsTask(
			ExecutorServiceManager executorServiceManager,
			Collection<PsicquicService> psicquicServices, Organism referenceOrganism,
			List<BinaryInteraction> interactions
	) {
		super(executorServiceManager);
		this.psicquicServices = psicquicServices;
		this.referenceOrganism = referenceOrganism;
		this.interactions = interactions;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("PPiMapBuilder interactome query");
		taskMonitor.setProgress(0);

		// Fetch interactions in reference organism
		taskMonitor.setStatusMessage("Fetch PSICQUIC interactions...");

		// Prepare Psicquic requests
		PsicquicRequestBuilder requestBuilder = new PsicquicRequestBuilder(psicquicServices)
				.addGetByTaxon(referenceOrganism.getTaxId());

		// Fetch result
		this.interactions.addAll(ConcurrentListExecutor.getResults(
				requestBuilder.getPsicquicRequests(),
				executorServiceManager,
				new ProgressTaskMonitor(taskMonitor)
		));
	}

}

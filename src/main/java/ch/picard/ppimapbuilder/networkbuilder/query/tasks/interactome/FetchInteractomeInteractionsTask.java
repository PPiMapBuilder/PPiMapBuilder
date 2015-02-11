package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.List;

class FetchInteractomeInteractionsTask extends AbstractInteractionQueryTask {

	private final Organism referenceOrganism;
	private final List<BinaryInteraction> interactions;

	public FetchInteractomeInteractionsTask(
			ThreadedClientManager threadedClientManager,
			Organism referenceOrganism,
			List<BinaryInteraction> interactions
	) {
		super(threadedClientManager);
		this.referenceOrganism = referenceOrganism;
		this.interactions = interactions;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("PPiMapBuilder interactome query");
		taskMonitor.setProgress(0);

		// Fetch interactions in reference organism
		taskMonitor.setStatusMessage("Fetch PSICQUIC interactions...");
		final ThreadedPsicquicClient psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
		String query = "taxidA:"+ referenceOrganism.getTaxId() + " AND taxidB:" + referenceOrganism.getTaxId();
		interactions.addAll(psicquicClient.getByQuery(
				query,
				new ProgressTaskMonitor(taskMonitor)
		));
		threadedClientManager.unRegister(psicquicClient);
	}

}

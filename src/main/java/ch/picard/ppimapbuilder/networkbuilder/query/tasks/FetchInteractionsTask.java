package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class FetchInteractionsTask extends AbstractInteractionQueryTask {

	// Input
	private final List<Organism> allOrganisms;
	private final UniProtEntrySet interactorPool;

	// Output
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	public FetchInteractionsTask(
			ThreadedClientManager threadedClientManager,
			List<Organism> allOrganisms, UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		super(threadedClientManager);
		this.allOrganisms = allOrganisms;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch interactions in all organisms...");

		final int[] i = new int[]{0};
		new ConcurrentExecutor<Collection<EncoreInteraction>>(threadedClientManager.getExecutorServiceManager(), allOrganisms.size()) {
			@Override
			public Callable<Collection<EncoreInteraction>> submitRequests(final int index) {
				return new Callable<Collection<EncoreInteraction>>() {
					@Override
					public Collection<EncoreInteraction> call() throws Exception {
						final Organism organism = allOrganisms.get(index);

						//Get proteins in the current organism
						final Set<Protein> proteins = interactorPool.findProteinInOrganismWithReferenceEntry(organism).keySet();

						//Get secondary interactions
						ThreadedPsicquicClient psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
						List<BinaryInteraction> interactionsBinary = psicquicClient.getInteractionsInProteinPool(proteins, organism);
						threadedClientManager.unRegister(psicquicClient);

						//Filter non uniprot and non current organism
						interactionsBinary = InteractionUtils.filter(
								interactionsBinary,
								new InteractionUtils.UniProtInteractionFilter(),
								new InteractionUtils.OrganismInteractionFilter(organism)
						);

						//Cluster
						return InteractionUtils.clusterInteraction(
								interactionsBinary
						);
					}
				};
			}

			@Override
			public void processResult(Collection<EncoreInteraction> result, Integer index) {
				taskMonitor.setProgress(((double)i[0]++)/((double)allOrganisms.size()));
				interactionsByOrg.put(allOrganisms.get(index), result);
			}

		}.run();

		//Free memory
		threadedClientManager.clear();
	}

}

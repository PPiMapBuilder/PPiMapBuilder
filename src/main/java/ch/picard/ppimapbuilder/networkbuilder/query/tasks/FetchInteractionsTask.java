package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class FetchInteractionsTask extends AbstractInteractionQueryTask {

	// Input
	private final List<Organism> allOrganisms;
	private final UniProtEntrySet interactorPool;

	// Output
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	private final ExecutorService executorService ;

	public FetchInteractionsTask(
			ThreadedClientManager threadedClientManager,
			List<Organism> allOrganisms, UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		super(threadedClientManager);
		this.allOrganisms = allOrganisms;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;

		this.executorService = threadedClientManager.getOrCreateThreadPool();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch interactions in all organisms...");

		final int[] i = new int[]{0};
		new ConcurrentExecutor<SecondaryInteractionQuery>(threadedClientManager.getExecutorServiceManager(), allOrganisms.size()) {
			@Override
			public Callable<SecondaryInteractionQuery> submitRequests(int index) {
				return new SecondaryInteractionQuery(
						allOrganisms.get(index), interactorPool,
						threadedClientManager
				);
			}

			@Override
			public void processResult(SecondaryInteractionQuery result, Integer index) {
				taskMonitor.setProgress(((double)i[0]++)/((double)allOrganisms.size()));
				interactionsByOrg.put(result.getOrganism(), result.getInteractions());
			}

		}.run();

		//Free memory
		threadedClientManager.clear();
	}

}

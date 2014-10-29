package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import org.cytoscape.work.TaskMonitor;

import java.util.List;
import java.util.concurrent.Callable;

public class FetchDirectInteractorOrtherOrganismsTask extends AbstractInteractionQueryTask {

	// Input
	private final List<Organism> otherOrganisms;
	private final Organism referenceOrganism;
	private final Double MINIMUM_ORTHOLOGY_SCORE;
	private final UniProtEntrySet proteinOfInterestPool;

	// Output
	private final UniProtEntrySet interactorPool;

	public FetchDirectInteractorOrtherOrganismsTask(
			ThreadedClientManager threadedClientManager,
			List<Organism> otherOrganisms, Organism referenceOrganism, Double minimum_orthology_score, UniProtEntrySet proteinOfInterestPool,
			UniProtEntrySet interactorPool
	) {
		super(threadedClientManager);
		this.otherOrganisms = otherOrganisms;
		this.referenceOrganism = referenceOrganism;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch interactors of input proteins in other organisms...");

		final UniProtEntrySet newInteractors = new UniProtEntrySet(referenceOrganism);
		final int[] i = new int[]{0};
		new ConcurrentExecutor<PrimaryInteractionQuery>(threadedClientManager.getExecutorServiceManager(), otherOrganisms.size()) {

			@Override
			public Callable<PrimaryInteractionQuery> submitRequests(int index) {
				return new PrimaryInteractionQuery(
						referenceOrganism, otherOrganisms.get(index), proteinOfInterestPool, interactorPool,
						threadedClientManager, MINIMUM_ORTHOLOGY_SCORE
				);
			}

			@Override
			public void processResult(PrimaryInteractionQuery result, Integer index) {
				taskMonitor.setProgress(((double)i[0]++)/((double)otherOrganisms.size()));
				newInteractors.addAll(result.getNewInteractors());
			}

		}.run();

		interactorPool.addAll(newInteractors);
	}

}

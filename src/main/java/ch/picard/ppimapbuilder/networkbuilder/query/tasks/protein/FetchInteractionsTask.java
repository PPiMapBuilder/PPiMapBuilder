package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.InteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.OrganismInteractorFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.UniProtInteractorFilter;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentFetcherIterator;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.task.AbstractThreadedTask;
import com.google.common.collect.Iterators;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;
import java.util.concurrent.Callable;

class FetchInteractionsTask extends AbstractThreadedTask {

	private final Collection<PsicquicService> psicquicServices;

	// Input
	private final List<Organism> allOrganisms;
	private final UniProtEntrySet interactorPool;
	private final Set<UniProtEntry> proteinOfInterestPool;

	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;
	// Output
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;

	public FetchInteractionsTask(
			ExecutorServiceManager webServiceClientFactory,
			Collection<PsicquicService> psicquicServices, List<Organism> allOrganisms, UniProtEntrySet interactorPool, Set<UniProtEntry> proteinOfInterestPool,
			HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		super(webServiceClientFactory);
		this.psicquicServices = psicquicServices;
		this.allOrganisms = allOrganisms;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.directInteractionsByOrg = directInteractionsByOrg;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch secondary interactions in all organisms...");

		final double size = allOrganisms.size();
		final double[] progressPercent = new double[]{0, 0};
		taskMonitor.setProgress(progressPercent[1] = 0d);
		new ConcurrentExecutor<Collection<EncoreInteraction>>(executorServiceManager, allOrganisms.size()) {
			@Override
			public Callable<Collection<EncoreInteraction>> submitRequests(final int index) {
				return new Callable<Collection<EncoreInteraction>>() {
					@Override
					public Collection<EncoreInteraction> call() throws Exception {
						final Organism organism = allOrganisms.get(index);

						//Get proteins in the current organism
						final Set<String> proteins = interactorPool.identifiersInOrganism(organism).keySet();
						//proteins.removeAll(interactorPool.identifiersInOrganism(proteinOfInterestPool, organism));

						//Get secondary interactions
						ThreadedPsicquicClient psicquicClient = new ThreadedPsicquicClient(psicquicServices, executorServiceManager);
						List<BinaryInteraction> interactionsBinary = psicquicClient.getInteractionsInProteinPool(proteins, organism);

						//Filter non uniprot and non current organism
						interactionsBinary = InteractionUtils.filterConcurrently(
								executorServiceManager,
								interactionsBinary,
								null,
								new UniProtInteractorFilter(),
								new OrganismInteractorFilter(organism)
						);

						//Add primary interactions
						interactionsBinary.addAll(directInteractionsByOrg.get(organism));

						//Cluster
						return InteractionUtils.clusterInteraction(
								interactionsBinary
						);
					}
				};
			}

			@Override
			public void processResult(Collection<EncoreInteraction> result, Integer index) {
				double percent = progressPercent[0]++ / size;
				if(percent > progressPercent[1])
					taskMonitor.setProgress(progressPercent[1] = percent);
				interactionsByOrg.put(allOrganisms.get(index), result);
			}

		}.run();
	}

}

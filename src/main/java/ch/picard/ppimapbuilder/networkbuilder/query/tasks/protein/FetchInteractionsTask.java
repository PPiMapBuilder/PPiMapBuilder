package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRequestBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
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
		taskMonitor.setProgress(0d);
		new ConcurrentExecutor<Collection<EncoreInteraction>>(executorServiceManager, allOrganisms.size()) {
			double num = 0d;
			double progress = 0d;

			@Override
			public Callable<Collection<EncoreInteraction>> submitRequests(final int index) {
				return new Callable<Collection<EncoreInteraction>>() {
					@Override
					public Collection<EncoreInteraction> call() throws Exception {
						final Organism organism = allOrganisms.get(index);

						// Get proteins in the current organism
						final Set<String> proteins = interactorPool.identifiersInOrganism(organism).keySet();
						//proteins.removeAll(interactorPool.identifiersInOrganism(proteinOfInterestPool, organism));

						// Prepare Psicquic requests
						final PsicquicRequestBuilder requestBuilder = new PsicquicRequestBuilder(psicquicServices)
								.addGetByProteinPool(proteins, organism.getTaxId());

						// Get concurrent iterator of BinaryInteraction
						final Iterator<BinaryInteraction> interactionIterator = new ConcurrentFetcherIterator<BinaryInteraction>(
								requestBuilder.getPsicquicRequests(),
								executorServiceManager
						);

						//Combined interaction filter. Filtering out non uniprot and non current organism
						final InteractionFilter filter = InteractionUtils.combineFilters(
								new UniProtInteractorFilter(),
								new OrganismInteractorFilter(organism)
						);

						// Interaction object stream = Fetch interactions > Filter interactions > Cluster interactions
						return InteractionUtils.clusterInteraction(
								Iterators.filter(
										interactionIterator,
										filter
								)
						);
					}
				};
			}

			@Override
			public void processResult(Collection<EncoreInteraction> intermediaryResult, Integer index) {
				double percent = ++num / size;
				if(percent > progress)
					taskMonitor.setProgress(progress = percent);
				interactionsByOrg.put(allOrganisms.get(index), intermediaryResult);
			}

		}.run();

		taskMonitor.setProgress(1.0);
	}

}

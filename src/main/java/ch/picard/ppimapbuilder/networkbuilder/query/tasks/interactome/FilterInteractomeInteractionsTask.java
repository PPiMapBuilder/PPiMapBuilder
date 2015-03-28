package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.networkbuilder.query.tasks.AbstractInteractionQueryTask;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class FilterInteractomeInteractionsTask extends AbstractInteractionQueryTask {
	private final List<BinaryInteraction> interactions;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final Organism referenceOrganism;

	public FilterInteractomeInteractionsTask(
			ExecutorServiceManager executorServiceManager,
			Organism referenceOrganism, List<BinaryInteraction> interactions,
			UniProtEntrySet interactorPool, HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		super(executorServiceManager);
		this.interactions = interactions;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
		this.referenceOrganism = referenceOrganism;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		// Filter interactions
		taskMonitor.setStatusMessage("Filter interactions...");
		final List<BinaryInteraction> filteredInteractions =
				InteractionUtils.filterConcurrently(
						executorServiceManager,
						interactions,
						new ProgressTaskMonitor(taskMonitor),

						new InteractionUtils.OrganismInteractionFilter(referenceOrganism),
						new InteractionUtils.UniProtInteractionFilter(),

						new InteractionUtils.InteractorFilter() {
							@Override
							public boolean isValidInteractor(Interactor interactor) {
								final Protein interactorProtein = InteractionUtils.getProteinInteractor(interactor);

								if(interactorProtein != null) {
									synchronized (interactorPool) {
										interactorPool.add(
												new UniProtEntry.Builder(interactorProtein).build()
										);
									}
									return true;
								}
								return false;
								/*boolean ok = false;
								String uniProtId = interactorProtein.getUniProtId();

								// Find in existing protein pools
								UniProtEntry entry = null;
								synchronized (interactorPool) {
									entry = interactorPool.findByPrimaryAccession(uniProtId);
								}

								// Find on UniProt
								if (entry == null) {
									try {
										entry = uniProtClient.retrieveProteinData(uniProtId);

										if (entry != null) synchronized (interactorPool) {
											interactorPool.add(entry);
										}
									} catch (IOException ignored) {
									}
								}

								if (entry != null) {
									ok = true;
								}
								return ok;*/
							}
						}
				);


		// Cluster interactions
		final Collection<EncoreInteraction> clusteredInteractions =
				InteractionUtils.clusterInteraction(
						filteredInteractions
				);

		// Save them
		interactionsByOrg.put(
				referenceOrganism,
				clusteredInteractions
		);
	}
}

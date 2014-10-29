package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import org.cytoscape.work.TaskMonitor;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PrimaryInteractionQuery implements Callable<PrimaryInteractionQuery> {

	private final Organism referenceOrganism;
	private final Organism organism;
	private final UniProtEntrySet proteinOfInterestPool;
	private final UniProtEntrySet proteinPool;

	private final ThreadedPsicquicClient psicquicClient;
	private final ThreadedProteinOrthologClientDecorator proteinOrthologClient;
	private final UniProtEntryClient uniProtClient;

	private final UniProtEntrySet newInteractors;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final ThreadedClientManager threadedClientManager;
	private final TaskMonitor taskMonitor;

	public PrimaryInteractionQuery(
			Organism referenceOrganism, Organism organism, UniProtEntrySet proteinOfInterestPool, UniProtEntrySet proteinPool,
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score
	) {
		this(
				referenceOrganism, organism, proteinOfInterestPool, proteinPool,
				threadedClientManager, minimum_orthology_score,
				null
		);
	}

	public PrimaryInteractionQuery(
			Organism referenceOrganism, Organism organism, UniProtEntrySet proteinOfInterestPool, UniProtEntrySet proteinPool,
			ThreadedClientManager threadedClientManager, Double minimum_orthology_score,
			TaskMonitor taskMonitor
	) {
		this.referenceOrganism = referenceOrganism;
		this.organism = organism;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.proteinPool = proteinPool;

		this.threadedClientManager = threadedClientManager;
		this.taskMonitor = taskMonitor;
		this.psicquicClient = threadedClientManager.getOrCreatePsicquicClient();
		this.proteinOrthologClient = threadedClientManager.getOrCreateProteinOrthologClient();
		this.uniProtClient = threadedClientManager.getOrCreateUniProtClient();

		MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;

		this.newInteractors = new UniProtEntrySet(organism);
	}

	public PrimaryInteractionQuery call() throws Exception {
		// Get list of protein of interest's orthologs in this organism
		Set<Protein> POIinOrg = proteinOfInterestPool.findProteinInOrganismWithReferenceEntry(organism).keySet();

		//Get primary interactors via interaction search
		ArrayList<String> queries = new ArrayList<String>();
		for (Protein protein : POIinOrg) {
			queries.add(
					InteractionUtils.generateMiQLQueryIDTaxID(protein.getUniProtId(), protein.getOrganism().getTaxId())
			);
		}

		final List<Protein> interactors = new ArrayList<Protein>(InteractionUtils.getInteractors(
				InteractionUtils.filter(
						psicquicClient.getByQueries(queries),
						new InteractionUtils.UniProtInteractionFilter(),
						new InteractionUtils.OrganismInteractionFilter(organism)
				)
		));
		threadedClientManager.unRegister(psicquicClient);

		//Remove POIs
		interactors.removeAll(POIinOrg);

		if (taskMonitor != null) taskMonitor.setProgress(0.66);

		//Search new interactors
		if (!interactors.isEmpty()) {
			if (organism.equals(referenceOrganism)) {
				UniProtEntryClient uniProtClient = threadedClientManager.getOrCreateUniProtClient();
				newInteractors.addAll(
						uniProtClient.retrieveProteinsData(
								ProteinUtils.asIdentifiers(interactors)
						).values(),
						false
				);
				threadedClientManager.unRegister(uniProtClient);
			} else {
				ExecutorService service = threadedClientManager.getExecutorServiceManager().createThreadPool(
						threadedClientManager.getExecutorServiceManager().getMaxNumberThread() * 2
				);
				new ConcurrentExecutor<UniProtEntry>(service, interactors.size()) {
					@Override
					public Callable<UniProtEntry> submitRequests(final int index) {
						return new Callable<UniProtEntry>() {
							@Override
							public UniProtEntry call() throws Exception {
								Protein interactor = interactors.get(index);

								Protein orthologInReferenceOrganism =
										proteinOrthologClient.getOrtholog(interactor, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);

								UniProtEntry entry = null;
								if (orthologInReferenceOrganism != null) {

									String uniProtId = orthologInReferenceOrganism.getUniProtId();
									entry = proteinPool.find(uniProtId);

									boolean found = entry != null;
									if (!found)
										entry = uniProtClient.retrieveProteinData(uniProtId);

									if (entry != null)
										entry.addOrtholog(interactor);

									if (found) entry = null;
								}
								return entry;
							}
						};
					}

					@Override
					public void processResult(UniProtEntry result, Integer index) {
						if (result != null) newInteractors.add(result, false);
					}
				}.run();
				threadedClientManager.getExecutorServiceManager().remove(service);
			}
		}

		threadedClientManager.unRegister(proteinOrthologClient);
		threadedClientManager.unRegister(uniProtClient);

		if (taskMonitor != null) taskMonitor.setProgress(1);

		return this;
	}

	public Organism getOrganism() {
		return organism;
	}

	public UniProtEntrySet getNewInteractors() {
		return newInteractors;
	}
}

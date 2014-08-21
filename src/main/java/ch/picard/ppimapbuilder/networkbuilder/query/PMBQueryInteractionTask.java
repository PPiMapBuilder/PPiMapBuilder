package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicSimpleClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.util.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.SteppedTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PMBQueryInteractionTask extends AbstractTask {

	// Clients
	final UniProtEntryClient uniProtEntryClient;
	final ThreadedPsicquicSimpleClient psicquicClient;
	final InParanoidClient inParanoidClient;
	final ThreadedProteinOrthologClient proteinOrthologClient;

	// Data input
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final List<Organism> otherOrganisms;
	private final List<Organism> allOrganisms;

	// Data output
	private final UniProtEntrySet proteinOfInterestPool; // not the same as user input
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

	// Thread list
	private final List<AbstractThreadedClient> threadedClients;
	private final ExecutorService executorService;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	public PMBQueryInteractionTask(
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			UniProtEntrySet interactorPool,
			UniProtEntrySet proteinOfInterestPool,
			QueryWindow qw
	) {
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		// Retrieve user input
		referenceOrganism = qw.getSelectedRefOrganism();
		inputProteinIDs = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		otherOrganisms = qw.getSelectedOrganisms();
		otherOrganisms.remove(referenceOrganism);
		allOrganisms = new ArrayList<Organism>();
		allOrganisms.addAll(otherOrganisms);
		allOrganisms.add(referenceOrganism);
		List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();

		final int STD_NB_THREAD =
			Math.min(2, Runtime.getRuntime().availableProcessors()) + 1;

		// Store thread pool used by web client and this task
		this.threadedClients = new ArrayList<AbstractThreadedClient>();
		this.executorService = Executors.newFixedThreadPool(STD_NB_THREAD);

		// Web Clients
		{
			// UniProt entry client
			threadedClients.add(uniProtEntryClient = new UniProtEntryClient());
			uniProtEntryClient.setMaxNumberThread(STD_NB_THREAD);

			// PSICQUIC Client
			threadedClients.add(psicquicClient = new ThreadedPsicquicSimpleClient(selectedDatabases));
			psicquicClient.setMaxNumberThread(STD_NB_THREAD);

			// Hybrid Web/Cache ortholog client
			ProteinOrthologWebCachedClient webCached;
			{
				MINIMUM_ORTHOLOGY_SCORE = 0.85;

				// InParanoid Client
				inParanoidClient = new InParanoidClient();
				inParanoidClient.enableCache(true); //XML response cache

				// PMB ortholog cache client
				PMBProteinOrthologCacheClient cacheClient = null;
				try {
					cacheClient = PMBProteinOrthologCacheClient.getInstance();
				} catch (IOException e) {
					e.printStackTrace();
				}

				webCached = new ProteinOrthologWebCachedClient(inParanoidClient, cacheClient);
			}
			ThreadedProteinOrthologClientDecorator<ProteinOrthologWebCachedClient> orthologClient
					= new ThreadedProteinOrthologClientDecorator<ProteinOrthologWebCachedClient>(webCached);
			proteinOrthologClient = orthologClient;
			threadedClients.add(orthologClient);
			orthologClient.setMaxNumberThread(STD_NB_THREAD);
		}
	}

	/**
	 * Complex network querying using PSICQUIC and InParanoid
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// 7 steps for the task
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, 5.0);
		monitor.setTitle("PPiMapBuilder interaction query");

		interactionsByOrg.clear();
		interactorPool.clear();

		monitor.setStep("Fetch UniProt entry data for input proteins...");
		initProteinOfInterestPool();

		//Ref organism get primary interactors
		monitor.setStep("Fetch interactors of input proteins in reference organism...");
		{
			interactorPool.addAll(new PrimaryInteractionQuery(
					referenceOrganism, referenceOrganism, proteinOfInterestPool, interactorPool,
					proteinOrthologClient, psicquicClient, uniProtEntryClient,
					MINIMUM_ORTHOLOGY_SCORE).call().getNewInteractors());
		}

		if(cancelled) return;

		//Fetch orthologs of interactor pool in other organisms
		monitor.setStep("Fetch interactors orthologs in other organisms...");
		proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(interactorPool, otherOrganisms, MINIMUM_ORTHOLOGY_SCORE);

		if(cancelled) return;

		//Other organisms get primary interactors
		monitor.setStep("Fetch interactors of input proteins in other organisms...");
		{
			//Requests
			final UniProtEntrySet newInteractors = new UniProtEntrySet();
			new ConcurrentExecutor<PrimaryInteractionQuery>(executorService, otherOrganisms.size()) {

				@Override
				public Callable<PrimaryInteractionQuery> submitRequests(int index) {
					return new PrimaryInteractionQuery(
							referenceOrganism, otherOrganisms.get(index), proteinOfInterestPool, interactorPool,
							proteinOrthologClient, psicquicClient, uniProtEntryClient,
							MINIMUM_ORTHOLOGY_SCORE
					);
				}

				@Override
				public void processResult(PrimaryInteractionQuery result) {
					newInteractors.addAll(result.getNewInteractors());
				}

			}.run();
			interactorPool.addAll(newInteractors);
		}

		if(cancelled) return;

		//Interaction search in protein pool for all organisms
		monitor.setStep("Fetch interactions in all organisms...");
		{
			new ConcurrentExecutor<SecondaryInteractionQuery>(executorService, allOrganisms.size()) {
				@Override
				public Callable<SecondaryInteractionQuery> submitRequests(int index) {
					return new SecondaryInteractionQuery(
							allOrganisms.get(index), interactorPool,
							psicquicClient
					);
				}

				@Override
				public void processResult(SecondaryInteractionQuery result) {
					interactionsByOrg.put(result.getOrganism(), result.getInteractions());
				}

			}.run();
		}

		//Free memory
		inParanoidClient.enableCache(false);
		System.gc();
	}

	/**
	 * Search UniProtEntry data for user input and convert all protein to ref org protein (if possible)
	 */
	private void initProteinOfInterestPool() {
		HashMap<String, UniProtEntry> uniProtEntries = uniProtEntryClient.retrieveProteinsData(inputProteinIDs);
		for (final String proteinID : inputProteinIDs) {
			UniProtEntry entry = uniProtEntries.get(proteinID);

			if (entry != null) {
				if(!entry.getOrganism().equals(referenceOrganism) &&
					entry.getOrganism().sameSpecies(referenceOrganism)
				) {
					entry = new UniProtEntry.Builder(entry)
							.setOrganism(referenceOrganism)
							.build();
				}

				if(!entry.getOrganism().equals(referenceOrganism)) {
					try {
						Protein ortholog = proteinOrthologClient.getOrtholog(entry, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
						entry = uniProtEntryClient.retrieveProteinData(ortholog.getUniProtId());
					} catch (Exception e) {
						entry = null;
					}
				}
			}

			if (entry == null) {
				System.err.println(proteinID + " was not found on UniProt in the reference organism.");
				//TODO : warn the user
				continue;
			}

			// Save the protein into the interactor pool
			proteinOfInterestPool.add(entry);
			interactorPool.add(entry);
		}
	}

	@Override
	public void cancel() {
		for (AbstractThreadedClient threadedClient : threadedClients) {
			for (ExecutorService executorService : threadedClient.getExecutorServices()) {
				if (!executorService.isShutdown() && !executorService.isTerminated())
					executorService.shutdownNow();
			}
		}
		if (!executorService.isShutdown() && !executorService.isTerminated())
			executorService.shutdownNow();
		interactionsByOrg.clear();
		interactorPool.clear();
		Thread.currentThread().interrupt();
	}
}

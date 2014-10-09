package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
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
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.util.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.SteppedTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PMBQueryInteractionTask extends AbstractTask {

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
	private final ThreadedClientManager threadedClientManager;
	private final ExecutorService executorService;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	// Clients
	private final WeakReference<ThreadedProteinOrthologClient> proteinOrthologClient;

	public PMBQueryInteractionTask(
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg,
			UniProtEntrySet interactorPool,
			UniProtEntrySet proteinOfInterestPool,
			NetworkQueryParameters networkQueryParameters
	) {
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		// Retrieve user input
		referenceOrganism = networkQueryParameters.getReferenceOrganism();
		inputProteinIDs = new ArrayList<String>(new HashSet<String>(networkQueryParameters.getProteinOfInterestUniprotId()));
		otherOrganisms = networkQueryParameters.getOtherOrganisms();
		otherOrganisms.remove(referenceOrganism);
		allOrganisms = new ArrayList<Organism>();
		allOrganisms.addAll(otherOrganisms);
		allOrganisms.add(referenceOrganism);
		List<PsicquicService> selectedDatabases = networkQueryParameters.getSelectedDatabases();

		final Integer STD_NB_THREAD = Math.min(2, Runtime.getRuntime().availableProcessors()) + 1;

		// Store thread pool used by web client and this task
		this.threadedClientManager = new ThreadedClientManager(STD_NB_THREAD, selectedDatabases);
		this.proteinOrthologClient = new WeakReference<ThreadedProteinOrthologClient>(threadedClientManager.registerProteinOrthologClient());

		this.executorService = STD_NB_THREAD == null ? null : Executors.newFixedThreadPool(STD_NB_THREAD);

		MINIMUM_ORTHOLOGY_SCORE = 0.85;
	}

	/**
	 * Complex network querying using PSICQUIC, InParanoid and UniProt
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
			interactorPool.addAll(
					new PrimaryInteractionQuery(
							referenceOrganism, referenceOrganism, proteinOfInterestPool, interactorPool,
							threadedClientManager, MINIMUM_ORTHOLOGY_SCORE
					).call().getNewInteractors()
			);
		}

		if (cancelled) return;

		//Fetch orthologs of interactor pool in other organisms
		monitor.setStep("Fetch orthologs of interactors in other organisms...");
		{
			//optimization : searching orthologs in a number of organisms limited to
			// the maximum organism in memory cache
			int step = PMBProteinOrthologCacheClient.MAX_NB_MEMORY_CACHE - 1;

			ThreadedProteinOrthologClient poc = proteinOrthologClient.get();
			if (poc != null) {
				for (int i = 0; i < otherOrganisms.size(); i += step) {
					poc.getOrthologsMultiOrganismMultiProtein(
							interactorPool,
							otherOrganisms.subList(
									i,
									Math.min(
											otherOrganisms.size(),
											i + step
									)
							),
							MINIMUM_ORTHOLOGY_SCORE
					);
				}
			}
		}

		if (cancelled) return;

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
							threadedClientManager, MINIMUM_ORTHOLOGY_SCORE
					);
				}

				@Override
				public void processResult(PrimaryInteractionQuery result, Integer index) {
					newInteractors.addAll(result.getNewInteractors());
				}

			}.run();
			interactorPool.addAll(newInteractors);
		}

		if (cancelled) return;

		//Interaction search in protein pool for all organisms
		monitor.setStep("Fetch interactions in all organisms...");
		{
			new ConcurrentExecutor<SecondaryInteractionQuery>(executorService, allOrganisms.size()) {
				@Override
				public Callable<SecondaryInteractionQuery> submitRequests(int index) {
					return new SecondaryInteractionQuery(
							allOrganisms.get(index), interactorPool,
							threadedClientManager
					);
				}

				@Override
				public void processResult(SecondaryInteractionQuery result, Integer index) {
					interactionsByOrg.put(result.getOrganism(), result.getInteractions());
				}

			}.run();
		}

		//Free memory
		threadedClientManager.clear();
		System.gc();
	}

	/**
	 * Search UniProtEntry data for user input and convert all protein to ref org protein (if possible)
	 */
	private void initProteinOfInterestPool() {
		UniProtEntryClient uniProtEntryClient = threadedClientManager.registerUniProtClient();

		HashMap<String, UniProtEntry> uniProtEntries = uniProtEntryClient.retrieveProteinsData(inputProteinIDs);
		for (final String proteinID : inputProteinIDs) {
			UniProtEntry entry = uniProtEntries.get(proteinID);

			if (entry != null) {
				if (
						!entry.getOrganism().equals(referenceOrganism) &&
						entry.getOrganism().sameSpecies(referenceOrganism)
				) {
					entry = new UniProtEntry.Builder(entry)
							.setOrganism(referenceOrganism)
							.build();
				}

				if (!entry.getOrganism().equals(referenceOrganism)) {
					try {
						ThreadedProteinOrthologClient poc = proteinOrthologClient.get();
						if (poc != null) {
							Protein ortholog = poc.getOrtholog(entry, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
							entry = uniProtEntryClient.retrieveProteinData(ortholog.getUniProtId());
						}
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

		threadedClientManager.unRegister(uniProtEntryClient);
	}

	@Override
	public void cancel() {
		threadedClientManager.cancelExecution();
		if (!executorService.isShutdown() && !executorService.isTerminated())
			executorService.shutdownNow();
		interactionsByOrg.clear();
		interactorPool.clear();
	}

	/**
	 * Manages all threaded web service clients used in the querying of the network.
	 */
	class ThreadedClientManager {

		private final Integer STD_NB_THREAD;

		private final PMBProteinOrthologCacheClient proteinOrthologCacheClient;
		private final InParanoidClient inParanoidClient;
		private final List<PsicquicService> selectedDatabases;

		// Map of threaded client indexed by boolean defined if they are in use or not
		private final Map<Boolean, Set<AbstractThreadedClient>> clients;

		public ThreadedClientManager(final Integer STD_NB_THREAD, List<PsicquicService> selectedDatabases) {
			this.selectedDatabases = selectedDatabases;

			this.STD_NB_THREAD = STD_NB_THREAD;

			// InParanoid Client
			inParanoidClient = new InParanoidClient();
			inParanoidClient.enableCache(true); //XML response cache

			// PMB ortholog cache client
			proteinOrthologCacheClient = PMBProteinOrthologCacheClient.getInstance();

			this.clients = new HashMap<Boolean, Set<AbstractThreadedClient>>();
			this.clients.put(true, new HashSet<AbstractThreadedClient>());
			this.clients.put(false, new HashSet<AbstractThreadedClient>());
		}

		private <T> T getUnUsedClientByClass(Class<T> clientClass) {
			for (AbstractThreadedClient client : clients.get(false)) {
				if (clientClass.isInstance(client))
					return clientClass.cast(client);
			}
			return null;
		}

		private <T extends AbstractThreadedClient> T register(T client) {
			clients.get(false).remove(client);
			clients.get(true).add(client);
			return client;
		}

		public synchronized ThreadedProteinOrthologClientDecorator registerProteinOrthologClient() {
			ThreadedProteinOrthologClientDecorator client = getUnUsedClientByClass(ThreadedProteinOrthologClientDecorator.class);
			if (client == null) {
				client = new ThreadedProteinOrthologClientDecorator(
						new ProteinOrthologWebCachedClient(inParanoidClient, proteinOrthologCacheClient),
						STD_NB_THREAD
				);
			}
			return register(client);
		}

		public synchronized UniProtEntryClient registerUniProtClient() {
			UniProtEntryClient client = getUnUsedClientByClass(UniProtEntryClient.class);
			if (client == null) client = new UniProtEntryClient(STD_NB_THREAD);
			return register(client);
		}

		public synchronized ThreadedPsicquicClient registerPsicquicClient() {
			ThreadedPsicquicClient client = getUnUsedClientByClass(ThreadedPsicquicClient.class);
			if (client == null) client = new ThreadedPsicquicClient(selectedDatabases, STD_NB_THREAD);
			return register(client);
		}

		public synchronized void unRegister(AbstractThreadedClient client) {
			clients.get(true).remove(client);
			clients.get(false).add(client);
		}

		private void clear() {
			inParanoidClient.enableCache(false);
			proteinOrthologCacheClient.clearMemoryCache();
			clients.clear();
		}

		private void cancelExecution() {
			for (Set<AbstractThreadedClient> clients : this.clients.values()) {
				for (AbstractThreadedClient client : clients) {
					for (ExecutorService executorService : client.getExecutorServices()) {
						if (!executorService.isShutdown() && !executorService.isTerminated())
							executorService.shutdownNow();
					}
				}
			}
		}
	}
}

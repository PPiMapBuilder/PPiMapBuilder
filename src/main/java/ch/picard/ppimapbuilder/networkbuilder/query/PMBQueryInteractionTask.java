package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import ch.picard.ppimapbuilder.data.interaction.client.web.InteractionUtils;
import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicSimpleClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import ch.picard.ppimapbuilder.util.SteppedTaskMonitor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class PMBQueryInteractionTask extends AbstractTask {

	// Clients
	final UniProtEntryClient uniProtEntryClient;
	final ThreadedPsicquicSimpleClient psicquicClient;
	final InParanoidClient inParanoidClient;
	final ThreadedProteinOrthologClientDecorator<ProteinOrthologWebCachedClient> proteinOrthologClient;

	// Data input
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final List<Organism> otherOrganisms;
	private final List<PsicquicService> selectedDatabases;

	// Data output
	private final UniProtEntrySet proteinOfInterestPool; // not the same as user input
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntrySet interactorPool;

	// Thread list
	private final List<Thread> slaveThreads;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	public PMBQueryInteractionTask(HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg, UniProtEntrySet interactorPool, UniProtEntrySet proteinOfInterestPool, QueryWindow qw) {
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.proteinOfInterestPool = proteinOfInterestPool;

		// Retrieve user input
		referenceOrganism = qw.getSelectedRefOrganism();
		inputProteinIDs = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		selectedDatabases = qw.getSelectedDatabases();
		otherOrganisms = qw.getSelectedOrganisms();
		otherOrganisms.remove(referenceOrganism);

		// Thread factory to keep list of all threads used during process
		this.slaveThreads = new ArrayList<Thread>();
		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				slaveThreads.add(thread);
				return thread;
			}
		};

		// Clients
		{
			// UniProt entry client
			uniProtEntryClient = new UniProtEntryClient(3);
			uniProtEntryClient.setThreadFactory(threadFactory);

			// PSICQUIC Client
			psicquicClient = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);
			psicquicClient.setThreadFactory(threadFactory);

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
			proteinOrthologClient = new ThreadedProteinOrthologClientDecorator<ProteinOrthologWebCachedClient>(webCached, 5);
			proteinOrthologClient.setThreadFactory(threadFactory);
		}
	}

	/**
	 * Complex network querying using PSICQUIC and InParanoid
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// 7 steps for the task
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, 7.0);

		interactionsByOrg.clear();
		interactorPool.clear();

		System.out.println();

		monitor.setTitle("PPiMapBuilder interaction query in reference organism");
		/* ------------------------------------------------------------------------------------------
		 * PART ONE: search interaction in reference organism
		 * ------------------------------------------------------------------------------------------ */
		final List<BinaryInteraction> baseRefInteractions = new ArrayList<BinaryInteraction>();
		{
			interactionsByOrg.put(referenceOrganism, new ArrayList<EncoreInteraction>());

			// Search interaction of protein of interest in reference organism
			monitor.setStep("Retrieving UniProt entries for protein of interest...");
			{
				List<String> queries = new ArrayList<String>();

				HashMap<String, UniProtEntry> uniProtEntries = uniProtEntryClient.retrieveProteinsData(inputProteinIDs);

				for (final String proteinID : inputProteinIDs) {
					UniProtEntry entry = uniProtEntries.get(proteinID);

					if (entry != null && !entry.getOrganism().equals(referenceOrganism)) {
						try {
							Protein ortholog = proteinOrthologClient.getOrtholog(entry, referenceOrganism, MINIMUM_ORTHOLOGY_SCORE);
							entry = uniProtEntryClient.retrieveProteinData(ortholog.getUniProtId());
						} catch (Exception e) {
							entry = null;
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

					// Add MiQL query to search interactions of the protein
					queries.add(generateMiQLQueryIDTaxID(entry.getUniProtId(), referenceOrganism.getTaxId()));
				}
				//System.out.println(queries);

				// Get all primary interactions in reference organism
				monitor.setStep("Searching interaction for protein of interest...");
				baseRefInteractions.addAll(psicquicClient.getByQueries(queries));
				System.out.println("interactions: " + baseRefInteractions.size());
				InteractionUtils.filterNonUniprotInteractors(baseRefInteractions);
				System.out.println("after removing non-UniProt: " + baseRefInteractions.size());
			}
		}

		// Get protein UniProt entries of the interactor pool
		monitor.setStep("Retrieving UniProt entries of all interaction's interactors...");
		{
			// Get interactors across all interactions
			Set<String> referenceInteractorsIDs = InteractionUtils.getInteractorsBinary(baseRefInteractions);

			// Exclude proteins of interest
			referenceInteractorsIDs.removeAll(proteinOfInterestPool.getAllAsUniProtId());

			// Get UniProt entries
			HashMap<String, UniProtEntry> uniProtProteins = uniProtEntryClient.retrieveProteinsData(referenceInteractorsIDs);

			// Add them to the interactor pool
			interactorPool.addAll(uniProtProteins.values());
		}

		monitor.setTitle("PPiMapBuilder interaction query in other organism(s)");
		/* ------------------------------------------------------------------------------------------
		 * PART TWO: search interaction in other
		 * ------------------------------------------------------------------------------------------ */
		{
			// Get orthologs of interactors
			monitor.setStep("Searching interactors orthologs...");
			final Map<Protein, Map<Organism, OrthologScoredProtein>> orthologs = new HashMap<Protein, Map<Organism, OrthologScoredProtein>>();
			{
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: " + interactorPool.size());
				System.out.println("n# org: " + otherOrganisms.size());
				System.out.println(interactorPool);
				System.out.println(otherOrganisms);

				try {
					// Search protein orthologs of interactors in the pool
					orthologs.putAll(
							proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(
									new ArrayList<Protein>(interactorPool),
									otherOrganisms,
									MINIMUM_ORTHOLOGY_SCORE
							)
					);
				} catch (IOException e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, "InParanoid is currently unavailable");
						}
					});
					e.printStackTrace();
					return; // This line prevent the app to generate a network without inParanoid
				}
			}

			// Get interactions between orthologs (by organisms)
			monitor.setStep("Searching orthologs's interactions...");
			{
				class OrthologInteractionResult {
					final Organism organism;
					final Collection<EncoreInteraction> interactions;
					UniProtEntrySet newProts;

					public OrthologInteractionResult(Organism organism, Collection<EncoreInteraction> interactions) {
						super();
						this.organism = organism;
						this.interactions = interactions;
						newProts = new UniProtEntrySet();
					}

					public void add(UniProtEntry prot) {
						newProts.add(prot);
					}
				}

				final List<Future<OrthologInteractionResult>> requests = new ArrayList<Future<OrthologInteractionResult>>();
				final ExecutorService executor = Executors.newFixedThreadPool(3);
				final CompletionService<OrthologInteractionResult> completionService = new ExecutorCompletionService<OrthologInteractionResult>(executor);

				// For each non reference organisms
				for (final Organism organism : otherOrganisms) {
					requests.add(completionService.submit(new Callable<OrthologInteractionResult>() {
						@Override
						public OrthologInteractionResult call() throws Exception {
							OrthologInteractionResult result = null;

							// Get list of protein of interest's orthologs in this organism
							Set<Protein> proteinOfInterestsOrthologs = new HashSet<Protein>();
							for (UniProtEntry proteinOfInterest : proteinOfInterestPool) {
								final Protein ortho = proteinOfInterest.getOrtholog(organism);

								if (ortho != null)
									proteinOfInterestsOrthologs.add(ortho);
							}

							// Search interactions of orthologs of protein of interest
							final Set<Protein> orthologsInOrg = new HashSet<Protein>();
							List<BinaryInteraction> orthologsInteractions;
							{

								{// Primary interaction
									// Generate MiQL query
									final List<String> additionnalQueries = new ArrayList<String>();
									for (final Protein protein : proteinOfInterestsOrthologs) {
										additionnalQueries.add(
												generateMiQLQueryIDTaxID(
														protein.getUniProtId(),
														organism.getTaxId()
												)
										);
									}

									// PSICQUIC result
									orthologsInteractions = psicquicClient.getByQueries(additionnalQueries);
								}

								{// Secondary interactions

									// Get all orthologs in this organism (from previous ortholog search)
									for (Map<Organism, OrthologScoredProtein> ortho : orthologs.values()) {
										Protein protein = ortho.get(organism);
										if (protein != null) orthologsInOrg.add(protein);
									}

									//Remove POIs in orthologs list for interaction research
									orthologsInOrg.removeAll(proteinOfInterestsOrthologs);
									Set<String> currentInteractors = InteractionUtils.getInteractorsBinary(orthologsInteractions);
									Collection<Protein> translatedInteractors = ProteinUtils.newProteins(currentInteractors, organism);
									orthologsInOrg.addAll(translatedInteractors);

									// Search secondary interactions for orthologs found in this organism (without POI's orthologs)
									orthologsInteractions.addAll(
											InteractionUtils.getInteractionsInProteinPool(
													orthologsInOrg, organism, psicquicClient
											)
									);
								}
							}

							// Interaction filtering
							InteractionUtils.filterNonUniprotInteractors(orthologsInteractions);
							InteractionUtils.filterByOrganism(orthologsInteractions, organism);

							//Cluster orthologs's interactions
							final List<EncoreInteraction> interactionBetweenOrthologs = new ArrayList<EncoreInteraction>(InteractionUtils.clusterInteraction(orthologsInteractions));

							// Store interactions found for this organism
							result = new OrthologInteractionResult(organism, interactionBetweenOrthologs);
							//System.out.println("ORG:" + org.getTaxId() + " -> " + interactionBetweenOrthologs.size() + " interactions found");

							//TODO Validate this part:
							// Get new interactors => not seen in reference organism
							{
								// Get all interactors
								List<Protein> orthologInteractors = new ArrayList<Protein>();
								for (String ID : InteractionUtils.getInteractorsEncore(interactionBetweenOrthologs)) {
									final Protein protein = new Protein(ID, organism);
									// Add only unknown orthologs
									if (!orthologsInOrg.contains(protein) && !proteinOfInterestsOrthologs.contains(protein))
										orthologInteractors.add(new Protein(ID, organism));
								}

								System.out.println(
										"ORG:" + organism + " -> " + orthologsInOrg.size() + " proteins found" +
												" -> " + orthologInteractors.size() + " new protein found\n"
								);

								if (!orthologInteractors.isEmpty()) {
									//System.out.println(orthologInteractors);

									final Map<Protein, Map<Organism, OrthologScoredProtein>> orthologsMultipleProtein =
											proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(orthologInteractors, Arrays.asList(referenceOrganism), MINIMUM_ORTHOLOGY_SCORE);

									// Get UniProtProtein entry from reference organism
									for (Map<Organism, OrthologScoredProtein> vals : orthologsMultipleProtein.values()) {
										Protein protInRefOrg = vals.get(referenceOrganism);
										//System.out.print(protInRefOrg+", ");

										if (protInRefOrg != null && !interactorPool.contains(protInRefOrg)) {
											UniProtEntry uniProtInRefOrg = uniProtEntryClient.retrieveProteinData(protInRefOrg.getUniProtId());
											result.add(uniProtInRefOrg);

											// Search orthologs of theses new protein in reference organism
											proteinOrthologClient.getOrthologsMultiOrganism(uniProtInRefOrg, otherOrganisms, MINIMUM_ORTHOLOGY_SCORE);
										}
									}
									System.out.println();
								}
							}
							return result;
						}
					}));
				}

				for (Future<OrthologInteractionResult> future : requests) {
					try {
						Future<OrthologInteractionResult> fut = completionService.take();
						OrthologInteractionResult res = fut.get();
						if (res != null) {
							interactionsByOrg.put(res.organism, res.interactions);
							if (!res.newProts.isEmpty())
								interactorPool.addAll(res.newProts);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		if (!baseRefInteractions.isEmpty()) {
			// Filter non uniprot protein interaction
			InteractionUtils.filterNonUniprotInteractors(baseRefInteractions);
			InteractionUtils.filterByOrganism(baseRefInteractions, referenceOrganism);

			// Add secondary interactions
			monitor.setStep("Searching secondary interactions in reference interactions...");
			{
				baseRefInteractions.addAll(InteractionUtils.getInteractionsInProteinPool(
						new HashSet<Protein>(interactorPool), referenceOrganism, psicquicClient
				));
			}

			// Remove duplicate interactions
			monitor.setStep("Clustering interactions in reference organism...");
			interactionsByOrg.get(referenceOrganism).addAll(InteractionUtils.clusterInteraction(baseRefInteractions));
		}

		//Free memory
		inParanoidClient.enableCache(false);
		slaveThreads.clear();
		System.gc();
	}

	private String generateMiQLQueryIDTaxID(final String id, final Integer taxId) {
		return new MiQLExpressionBuilder() {{
			setRoot(true);
			add(new MiQLParameterBuilder("taxidA", taxId));
			addCondition(Operator.AND, new MiQLParameterBuilder("taxidB", taxId));
			addCondition(Operator.AND, new MiQLParameterBuilder("id", id));
		}}.toString();
	}

	@Override
	public void cancel() {
		for (Thread thread : slaveThreads)
			if (thread.isAlive() && !thread.isInterrupted() && !thread.getState().equals(Thread.State.TERMINATED))
				thread.interrupt();
		interactionsByOrg.clear();
		interactorPool.clear();
		Thread.currentThread().interrupt();
	}
}

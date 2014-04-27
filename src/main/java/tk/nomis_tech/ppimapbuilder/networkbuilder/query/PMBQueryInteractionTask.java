package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.data.client.ProteinOrthologWebCachedClient;
import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.ProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.InteractionUtils;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.PsicquicService;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.ThreadedPsicquicSimpleClient;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.miql.MiQLParameterBuilder;
import tk.nomis_tech.ppimapbuilder.data.client.web.ortholog.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.data.client.web.protein.UniProtEntryClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntryCollection;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
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
	final ProteinOrthologWebCachedClient proteinOrthologClient;

	// Data input
	private final List<String> inputProteinIDs;
	private final Organism referenceOrganism;
	private final List<Organism> otherOrganisms;
	private final List<PsicquicService> selectedDatabases;

	// Data output
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntryCollection interactorPool;

	// Thread list
	private final List<Thread> slaveThreads;

	// Steps
	private final double NB_STEP;
	private int currentStep;

	public PMBQueryInteractionTask(HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg, UniProtEntryCollection interactorPool, QueryWindow qw) {
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;

		this.NB_STEP = 7.0;
		this.currentStep = 0;

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
			proteinOrthologClient = new ProteinOrthologWebCachedClient();
			{
				// InParanoid Client
				inParanoidClient = new InParanoidClient(5, 0.85);
				inParanoidClient.enableCache(true); //XML response cache
				inParanoidClient.setThreadFactory(threadFactory);

				// PMB ortholog cache client
				try {
					ProteinOrthologCacheClient proteinOrthologCacheClient = ProteinOrthologCacheClient.getInstance();
					proteinOrthologCacheClient.setThreadFactory(threadFactory);
					proteinOrthologClient.setCacheClient(proteinOrthologCacheClient);
				} catch (IOException e) {
					e.printStackTrace();
				}

				proteinOrthologClient.setWebClient(inParanoidClient);
				proteinOrthologClient.setThreadFactory(threadFactory);
			}
		}
	}

	/**
	 * Complex network querying using PSICQUIC and InParanoid
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		interactionsByOrg.clear();
		interactorPool.clear();

		System.out.println();

		monitor.setTitle("PPiMapBuilder interaction query in reference organism");
		/* ------------------------------------------------------------------------------------------
		 * PART ONE: search interaction in reference organism
		 * ------------------------------------------------------------------------------------------ */
		final List<BinaryInteraction> baseRefInteractions = new ArrayList<BinaryInteraction>();
		final UniProtEntryCollection proteinOfInterests = new UniProtEntryCollection();
		{
			interactionsByOrg.put(referenceOrganism, new ArrayList<EncoreInteraction>());

			// Search interaction of protein of interest in reference organism
			changeStep("Retrieving UniProt entries for protein of interest...", monitor);
			{
				List<String> queries = new ArrayList<String>();

				HashMap<String, UniProtEntry> uniProtEntries = uniProtEntryClient.retrieveProteinsData(inputProteinIDs);

				for (final String proteinID : inputProteinIDs) {
					UniProtEntry entry = uniProtEntries.get(proteinID);

					if (entry != null && !entry.getOrganism().equals(referenceOrganism)) {
						try {
							Protein ortholog = proteinOrthologClient.getOrtholog(entry, referenceOrganism);
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
					proteinOfInterests.add(entry);
					interactorPool.add(entry);

					// Add MiQL query to search interactions of the protein
					queries.add(generateMiQLQueryIDTaxID(proteinID, referenceOrganism.getTaxId()));
				}
				//System.out.println(queries);

				// Get all primary interactions in reference organism
				changeStep("Searching interaction for protein of interest...", monitor);
				baseRefInteractions.addAll(psicquicClient.getByQueries(queries));
				System.out.println("interactions: " + baseRefInteractions.size());
				InteractionUtils.filterNonUniprotInteractors(baseRefInteractions);
				System.out.println("after removing non-UniProt: " + baseRefInteractions.size());
			}
		}

		// Get protein UniProt entries of the interactor pool
		changeStep("Retrieving UniProt entries of all interaction's interactors...", monitor);
		{
			// Get interactors across all interactions
			Set<String> referenceInteractorsIDs = InteractionUtils.getInteractorsBinary(baseRefInteractions);

			// Exclude proteins of interest
			referenceInteractorsIDs.removeAll(proteinOfInterests.getAllAsUniProtId());

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
			changeStep("Searching interactors orthologs...", monitor);
			final Map<Protein, Map<Organism, Protein>> orthologs = new HashMap<Protein, Map<Organism, Protein>>();
			{
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: " + interactorPool.size());
				System.out.println("n# org: " + otherOrganisms.size());

				try {
					// Search protein orthologs of interactors in the pool
					orthologs.putAll(
							proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(
									new ArrayList<Protein>(interactorPool),
									otherOrganisms
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
			changeStep("Searching orthologs's interactions...", monitor);
			{
				class OrthologInteractionResult {
					final Organism organism;
					final Collection<EncoreInteraction> interactions;
					UniProtEntryCollection newProts;

					public OrthologInteractionResult(Organism organism, Collection<EncoreInteraction> interactions) {
						super();
						this.organism = organism;
						this.interactions = interactions;
						newProts = new UniProtEntryCollection();
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
							for (UniProtEntry proteinOfInterest : proteinOfInterests) {
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
									for (Map<Organism, Protein> ortho : orthologs.values()) {
										Protein protein = ortho.get(organism);
										if (protein != null) orthologsInOrg.add(protein);
									}

									//Remove POIs in orthologs list for interaction research
									orthologsInOrg.removeAll(proteinOfInterestsOrthologs);

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
							final List<EncoreInteraction> interactionBetweenOrthologs = new ArrayList(InteractionUtils.clusterInteraction(orthologsInteractions));

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

									final Map<Protein, Map<Organism, Protein>> orthologsMultipleProtein =
											proteinOrthologClient.getOrthologsMultiOrganismMultiProtein(orthologInteractors, Arrays.asList(referenceOrganism));

									// Get UniProtProtein entry from reference organism
									for (Map<Organism, Protein> vals : orthologsMultipleProtein.values()) {
										Protein protInRefOrg = vals.get(referenceOrganism);
										//System.out.print(protInRefOrg+", ");

										if (protInRefOrg != null && !interactorPool.contains(protInRefOrg)) {
											UniProtEntry uniProtInRefOrg = uniProtEntryClient.retrieveProteinData(protInRefOrg.getUniProtId());
											result.add(uniProtInRefOrg);

											// Search orthologs of theses new protein in reference organism
											proteinOrthologClient.getOrthologsMultiOrganism(uniProtInRefOrg, otherOrganisms);
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
			changeStep("Searching secondary interactions in reference interactions...", monitor);
			{
				baseRefInteractions.addAll(InteractionUtils.getInteractionsInProteinPool(
						new HashSet<Protein>(interactorPool), referenceOrganism, psicquicClient
				));
			}

			// Remove duplicate interactions
			changeStep("Clustering interactions in reference organism...", monitor);
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

	private void changeStep(String message, TaskMonitor monitor) {
		monitor.setStatusMessage(message);
		monitor.setProgress(++currentStep / NB_STEP);
	}

}

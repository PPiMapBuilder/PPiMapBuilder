package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.data.client.web.interaction.InteractionsUtil;
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
import tk.nomis_tech.ppimapbuilder.data.protein.UniprotId;
import tk.nomis_tech.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class PMBQueryInteractionTask extends AbstractTask {

	private final double NB_STEP;
	// Data input
	private final QueryWindow qw;
	// Data output
	private final HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntryCollection interactorPool;
	private final PMBInteractionNetworkBuildTaskFactory pmbInteractionNetworkBuildTaskFactory;
	private int currentStep;
	private List<String> inputProteinIDs;

	public PMBQueryInteractionTask(PMBInteractionNetworkBuildTaskFactory pmbInteractionNetworkBuildTaskFactory, HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg, UniProtEntryCollection interactorPool, QueryWindow qw) {
		this.pmbInteractionNetworkBuildTaskFactory = pmbInteractionNetworkBuildTaskFactory;
		this.interactionsByOrg = interactionsByOrg;
		this.interactorPool = interactorPool;
		this.qw = qw;

		NB_STEP = 5.0;
		currentStep = 0;
	}

	/**
	 * Complex network querying using PSICQUIC
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		interactionsByOrg.clear();
		interactorPool.clear();

		// Retrieve user input
		final Organism refOrg = qw.getSelectedRefOrganism();
		final List<String> inputProteinIDs = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		final List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		final List<Organism> otherOrgs = qw.getSelectedOrganisms();
		otherOrgs.remove(refOrg);

		final ThreadedPsicquicSimpleClient psicquicClient = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);
		final InParanoidClient inParanoidClient = new InParanoidClient(5, 0.85);

		System.out.println();

		/* ------------------------------------------------------------------------------------------
		 * PART ONE: search interaction in reference organism
		 * ------------------------------------------------------------------------------------------ */
		List<BinaryInteraction> baseRefInteractions = new ArrayList<BinaryInteraction>();
		{
			interactionsByOrg.put(refOrg.getTaxId(), new ArrayList<EncoreInteraction>());

			// Search interaction of protein of interest in reference organism
			changeStep("Searching interaction for protein of interest", monitor);
			{
				List<String> queries = new ArrayList<String>();
				for (final String proteinID : inputProteinIDs) {
					if (!UniprotId.isValid(proteinID)) {
						System.err.println(proteinID + " is not a valid Uniprot ID.");
						continue;
					}

					UniProtEntry proteinEntry = UniProtEntryClient.getInstance().retrieveProteinData(proteinID);

					// If found in another organism than the reference organism
					if (proteinEntry != null && !proteinEntry.getOrganism().equals(refOrg)) {
						// Search in ref org
						try {
							Protein inRefOrg = inParanoidClient.getOrtholog(proteinEntry, refOrg);
							proteinEntry = UniProtEntryClient.getInstance().retrieveProteinData(inRefOrg.getUniProtId());
						} catch (IOException e) {
							continue;
						}
					}

					if (proteinEntry == null) {
						System.err.println(proteinID + " was not found on UniProt in the reference organism.");
						//TODO : warn the user
						continue;
					}

					interactorPool.add(proteinEntry);

					queries.add(generateMiQLQueryIDTaxID(proteinID, refOrg.getTaxId()));
				}
				//System.out.println(queries);
				baseRefInteractions.addAll(psicquicClient.getByQueries(queries));
			}
			System.out.println("interactions: " + baseRefInteractions.size());
		}

		// Get protein UniProt entries
		{
			// Get reference interactors
			Set<String> referenceInteractorsIDs = InteractionsUtil.getInteractorsBinary(baseRefInteractions, refOrg.getTaxId());
			referenceInteractorsIDs.removeAll(inputProteinIDs);

			HashMap<String, UniProtEntry> uniProtProteins = UniProtEntryClient.getInstance().retrieveProteinsData(referenceInteractorsIDs);
			interactorPool.addAll(uniProtProteins.values());
		}
		
		/* ------------------------------------------------------------------------------------------
		 * PART TWO: search interaction in other
		 * ------------------------------------------------------------------------------------------ */
		monitor.setTitle("PSICQUIC interaction query in other organism(s)");
		{
			final List<Integer> otherOrgsTaxIds = new ArrayList<Integer>();
			for (Organism org : otherOrgs)
				otherOrgsTaxIds.add(org.getTaxId());

			// Get orthologs of interactors
			changeStep("Searching interactors orthologs...", monitor);
			final Map<Protein, Map<Organism, Protein>> orthologs = new HashMap<Protein, Map<Organism, Protein>>();
			{
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: " + interactorPool.size());
				System.out.println("n# org: " + otherOrgsTaxIds.size());

				try {
					orthologs.putAll(inParanoidClient.getOrthologsMultiOrganismMultiProtein(new ArrayList<Protein>(interactorPool), otherOrgs));
				} catch (IOException e) {

					new Thread() {
						public void run() {
							JOptionPane.showMessageDialog(null, "InParanoid is currently unavailable");
						}
					}.start();
					e.printStackTrace();
					return; // This line prevent the app to generate a network without inparanoid
				} finally {
				}
			}

			// Get organism interactions
			changeStep("Searching orthologs's interactions...", monitor);
			{
				// TODO maybe use thread on this for loop
				class OrthologInteractionResult {
					final int taxId;
					final Collection<EncoreInteraction> interactions;
					UniProtEntryCollection newProts;

					public OrthologInteractionResult(int taxId, Collection<EncoreInteraction> interactions) {
						super();
						this.taxId = taxId;
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

				// For each other organism
				for (final Organism org : otherOrgs) {

					requests.add(completionService.submit(new Callable<OrthologInteractionResult>() {
						@Override
						public OrthologInteractionResult call() throws Exception {
							OrthologInteractionResult result = null;

							//Get list of uniprotIDs of othologs in this organism
							Set<Protein> orthologsInOrg = new HashSet<Protein>();
							for (Map<Organism, Protein> ortho : orthologs.values()) {
								Protein prot = ortho.get(org);
								if (prot != null) orthologsInOrg.add(prot);
							}

							//Get list of protein of interest's orthologs in this organism
							Set<String> POIorthologs = new HashSet<String>();
							for (String protID : inputProteinIDs) {
								UniProtEntry prot = interactorPool.find(protID);
								if (prot != null) {
									final Protein ortho = prot.getOrthologByTaxid(org.getTaxId());

									if (ortho != null)
										POIorthologs.add(ortho.getUniProtId());
								}
							}

							//Remove POIs in orthologs list for interaction research
							orthologsInOrg.removeAll(POIorthologs);

							//Find interactions of orthologs of protein of interest
							List<String> additionnalQueries = new ArrayList<String>();
							for (final String protID : POIorthologs) {
								additionnalQueries.add(generateMiQLQueryIDTaxID(protID, org.getTaxId()));
							}
							List<BinaryInteraction> orthologsInteractions = psicquicClient.getByQueries(additionnalQueries);

							// Search all interactions for orthologs found in this organism
							orthologsInteractions.addAll(InteractionsUtil.getInteractionsInProteinPool(orthologsInOrg, org, selectedDatabases));

							//Cluster orthologs's interactions
							Collection<EncoreInteraction> interactionBetweenOrthologs = InteractionsUtil.clusterInteraction(orthologsInteractions);

							// Store interactions found for this organism
							result = new OrthologInteractionResult(org.getTaxId(), interactionBetweenOrthologs);
							//System.out.println("ORG:" + org.getTaxId() + " -> " + interactionBetweenOrthologs.size() + " interactions found");

							//TODO Validate this part:
							// Get new interactors => not seen in reference organism
							{
								Set<String> orthologInteractors = InteractionsUtil.getInteractorsEncore(interactionBetweenOrthologs);
								orthologInteractors.removeAll(orthologsInOrg);
								orthologInteractors.removeAll(POIorthologs);

								System.out.println(
										"ORG:" + org.getTaxId() + " -> " + orthologsInOrg.size() + " proteins found" +
												" -> " + orthologInteractors.size() + " new protein found\n"
								);

								if (!orthologInteractors.isEmpty()) {
									//System.out.println(orthologInteractors);

									HashMap<String, HashMap<Integer, String>> orthologsMultipleProtein =
											inParanoidClient.getOrthologsMultipleProtein(orthologInteractors, Arrays.asList(refOrg.getTaxId()));

									// Get UniProtProtein entry from reference organism
									for (HashMap<Integer, String> vals : orthologsMultipleProtein.values()) {
										String protInRefOrg = vals.get(refOrg.getTaxId());
										//System.out.print(protInRefOrg+", ");

										if (protInRefOrg != null && !interactorPool.contains(protInRefOrg)) {
											UniProtEntry uniProtInRefOrg = UniProtEntryClient.getInstance().retrieveProteinData(protInRefOrg);
											result.add(uniProtInRefOrg);
											inParanoidClient.getOrthologsMultiOrganism(uniProtInRefOrg, otherOrgs);
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
							interactionsByOrg.put(res.taxId, res.interactions);
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
			baseRefInteractions = (List<BinaryInteraction>) InteractionsUtil.filterNonUniprotAndNonRefOrg(
					baseRefInteractions,
					refOrg.getTaxId()
			);

			// Add secondary interactions
			changeStep("Searching secondary interactions in reference interactions...", monitor);
			{
				baseRefInteractions.addAll(InteractionsUtil.getInteractionsInProteinPool(
						new HashSet<Protein>(interactorPool), refOrg, selectedDatabases
				));
			}

			// Remove duplicate interactions
			changeStep("Clustering interactions in reference organism...", monitor);
			interactionsByOrg.get(refOrg.getTaxId()).addAll(InteractionsUtil.clusterInteraction(baseRefInteractions));
		}
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
		Thread.currentThread().interrupt();
	}

	private void changeStep(String message, TaskMonitor monitor) {
		monitor.setStatusMessage(message);
		monitor.setProgress(++currentStep / NB_STEP);
	}

	public PMBInteractionNetworkBuildTaskFactory getPmbInteractionNetworkBuildTaskFactory() {
		return pmbInteractionNetworkBuildTaskFactory;
	}

}

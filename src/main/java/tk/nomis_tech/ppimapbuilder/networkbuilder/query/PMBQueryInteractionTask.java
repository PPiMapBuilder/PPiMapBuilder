package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntryCollection;
import tk.nomis_tech.ppimapbuilder.data.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.webservice.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.webservice.InteractionsUtil;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicService;
import tk.nomis_tech.ppimapbuilder.webservice.ThreadedPsicquicSimpleClient;
import tk.nomis_tech.ppimapbuilder.webservice.UniProtEntryClient;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLParameterBuilder;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBQueryInteractionTask extends AbstractTask {

	private final double NB_STEP;
	private int currentStep;

	// Data input
	private final QueryWindow qw;

	// Data output
	private final HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg;
	private final UniProtEntryCollection interactorPool;

	public PMBQueryInteractionTask(HashMap<Integer, Collection<EncoreInteraction>> interactionsByOrg, UniProtEntryCollection interactorPool, QueryWindow qw) {
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
		monitor.setTitle("PSICQUIC interaction query in reference organism");
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
					if(proteinEntry == null) {
						System.err.println(proteinID + " was not found on UniProt.");
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
			changeStep("Find interactors orthologs...", monitor);
			final HashMap<String, HashMap<Integer, String>> orthologs = new HashMap<String, HashMap<Integer, String>>();
			{
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: " + interactorPool.size());
				System.out.println("n# org: " + otherOrgsTaxIds.size());

				try {
					orthologs.putAll(inParanoidClient.searchOrthologForUniprotProtein(interactorPool, otherOrgsTaxIds));
				} finally {}
			}

			// Get ortholog interactions
			changeStep("Find orthologs's interactions...", monitor);
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
							Set<String> orthologsInOrg = new HashSet<String>();
							for (HashMap<Integer, String> ortho : orthologs.values()) {
								String id = ortho.get(org.getTaxId());
								if (id != null) orthologsInOrg.add(id);
							}
							
							//Get list of protein of interest's orthologs in this organism
							Set<String> POIorthologs = new HashSet<String>();
							for(String protID: inputProteinIDs) {
								UniProtEntry prot = interactorPool.find(protID);
								if(prot != null) {
									final OrthologProtein ortho = prot.getOrthologByTaxid(org.getTaxId());
									
									if(ortho != null)
										POIorthologs.add(ortho.getUniprotId());
								}
							}
							
							//Remove POIs in orthologs list for interaction research
							orthologsInOrg.removeAll(POIorthologs);

							//Find interactions of orthologs of protein of interest
							List<String> additionnalQueries = new ArrayList<String>();
							for(final String protID: POIorthologs) {
								additionnalQueries.add(generateMiQLQueryIDTaxID(protID, org.getTaxId()));
							}
							List<BinaryInteraction> orthologsInteractions = psicquicClient.getByQueries(additionnalQueries);
							
							// Search all interactions for orthologs found in this organism
							orthologsInteractions.addAll(InteractionsUtil.getInteractionsInProteinPool(orthologsInOrg, org.getTaxId(), selectedDatabases));

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
									" -> " + orthologInteractors.size() + " new protein found\n");

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
											inParanoidClient.searchOrthologForUniprotProtein(Arrays.asList(uniProtInRefOrg), otherOrgsTaxIds);
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
						if(res != null) {
							interactionsByOrg.put(res.taxId, res.interactions);
							if(!res.newProts.isEmpty())
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
					interactorPool.getAllAsUniProtId(), refOrg.getTaxId(), selectedDatabases
				));
			}

			// Remove duplicate interactions
			changeStep("Clustering interactions in reference organism...", monitor);
			interactionsByOrg.get(refOrg.getTaxId()).addAll(InteractionsUtil.clusterInteraction(baseRefInteractions));
		}
	}
	
	private String generateMiQLQueryIDTaxID(final String id, final Integer taxId) {
		return new MiQLExpressionBuilder(){{
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

}

package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntryCollection;
import tk.nomis_tech.ppimapbuilder.orthology.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.querywindow.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.webservice.InteractionsUtil;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicService;
import tk.nomis_tech.ppimapbuilder.webservice.ThreadedPsicquicSimpleClient;
import tk.nomis_tech.ppimapbuilder.webservice.UniProtEntryClient;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLParameterBuilder;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBQueryInteractionTask extends AbstractTask {

	private double NB_STEP;
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
		final List<String> proteinIDs = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		final List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		final List<Organism> otherOrgs = qw.getSelectedOrganisms();
		otherOrgs.remove(refOrg);

		ThreadedPsicquicSimpleClient psicquicClient = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);
		InParanoidClient inParanoidClient = new InParanoidClient(9, 0.85);

		System.out.println();

		/* ------------------------------------------------------------------------------------------
		 * PART ONE: search interaction in reference organism
		 * ------------------------------------------------------------------------------------------ */
		monitor.setTitle("PSICQUIC interaction query in reference organism");
		{
			interactionsByOrg.put(refOrg.getTaxId(), new ArrayList<EncoreInteraction>());
			List<BinaryInteraction> baseInteractions = new ArrayList<BinaryInteraction>();

			// Search interaction of protein of interest in reference organism
			changeStep("Searching interaction for protein of interest", monitor);
			{
				List<String> queries = new ArrayList<String>();
				for (final String proteinID : proteinIDs) {
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
					
					MiQLExpressionBuilder query = new MiQLExpressionBuilder(){{
						setRoot(true);
						add(new MiQLParameterBuilder("taxidA", refOrg.getTaxId()));
						addCondition(
							Operator.AND,
							new MiQLParameterBuilder("taxidB", refOrg.getTaxId())
						);
						addCondition(
							Operator.AND,
							new MiQLParameterBuilder("id", proteinID)
						);
					}};
					queries.add(query.toString());
				}
				baseInteractions.addAll(psicquicClient.getByQueries(queries));
			}
			System.out.println("interactions: " + baseInteractions.size());

			if (!baseInteractions.isEmpty()) {
				// Filter non uniprot protein interaction
				baseInteractions = (List<BinaryInteraction>) InteractionsUtil.filterNonUniprotAndNonRefOrg(baseInteractions,
						refOrg.getTaxId());

				// Add secondary interactions
				changeStep("Searching secondary interactions...", monitor);
				{
					// Find interactors list (without protein of interest)
					HashSet<String> interactors = new HashSet<String>(InteractionsUtil.getInteractorsBinary(baseInteractions,
							refOrg.getTaxId()));
					interactors.removeAll(proteinIDs);

					// Search interactions between interactors
					baseInteractions
							.addAll(InteractionsUtil.getInteractionBetweenProtein(interactors, refOrg.getTaxId(), selectedDatabases));
				}

				// Remove duplicate interactions
				changeStep("Removing interaction duplicates...", monitor);
				interactionsByOrg.get(refOrg.getTaxId()).addAll(InteractionsUtil.clusterInteraction(baseInteractions));
			}

		}

		// Get protein UniProt entries
		{
			// Get reference interactors
			Set<String> referenceInteractorsIDs = InteractionsUtil.getInteractorsEncore(interactionsByOrg.get(refOrg.getTaxId()));
			referenceInteractorsIDs.removeAll(proteinIDs);

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
				// TODO maybe search orthologs interactions using thread
				// For each other organism
				for (final Organism org : otherOrgs) {
					HashSet<String> prots = new HashSet<String>();
					for (HashMap<Integer, String> ortho : orthologs.values()) {
						String id = ortho.get(org.getTaxId());
						if (id != null)
							prots.add(id);
					}

					// Search all interactions for orthologs found in this organism
					Collection<EncoreInteraction> interactionBetweenOrthologs = InteractionsUtil.clusterInteraction(InteractionsUtil
							.getInteractionBetweenProtein(prots, org.getTaxId(), selectedDatabases));

					// Store interactions found for this organism
					interactionsByOrg.put(org.getTaxId(), interactionBetweenOrthologs);
					System.out.println("ORG:" + org.getTaxId() + " -> " + prots.size() + " proteins found");
					System.out.println("ORG:" + org.getTaxId() + " -> " + interactionBetweenOrthologs.size() + " interactions found");

					//TODO Validate this part:
					// Get new interactors => not seen in reference organism
					{
						Set<String> orthologInteractors = InteractionsUtil.getInteractorsEncore(interactionBetweenOrthologs);
						orthologInteractors.removeAll(prots);

						System.out.println("ORG:" + org.getTaxId() + " -> " + orthologInteractors.size() + " new protein found");

						if (!orthologInteractors.isEmpty()) {
							HashMap<String, HashMap<Integer, String>> orthologsMultipleProtein = inParanoidClient
									.getOrthologsMultipleProtein(orthologInteractors, Arrays.asList(refOrg.getTaxId()));

							// Get UniProtProtein entry from reference organism
							for (HashMap<Integer, String> vals : orthologsMultipleProtein.values()) {
								String protInRefOrg = vals.get(org.getTaxId());

								if (interactorPool.contains(protInRefOrg)) {
									UniProtEntry uniProtInRefOrg = UniProtEntryClient.getInstance().retrieveProteinData(protInRefOrg);
									interactorPool.add(uniProtInRefOrg);
									inParanoidClient.searchOrthologForUniprotProtein(Arrays.asList(uniProtInRefOrg),
											otherOrgsTaxIds);
								}
							}
						}
					}
					System.out.println("--");
				}
			}
		}
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

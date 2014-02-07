package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.networkbuilder.data.UniProtProtein;
import tk.nomis_tech.ppimapbuilder.networkbuilder.data.UniProtProteinCollection;
import tk.nomis_tech.ppimapbuilder.orthology.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.InteractionsUtil;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;
import tk.nomis_tech.ppimapbuilder.util.ThreadedPsicquicSimpleClient;
import tk.nomis_tech.ppimapbuilder.util.UniProtEntryClient;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLParameterBuilder;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLExpressionBuilder.Operator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBQueryInteractionTask extends AbstractTask {

	private final Collection<BinaryInteraction> interactionResults;
	private QueryWindow qw;
	private double NB_STEP;
	private int currentStep;

	public PMBQueryInteractionTask(Collection<BinaryInteraction> interactionResults, QueryWindow qw) {
		this.interactionResults = interactionResults;
		this.qw = qw;

		NB_STEP = 6.0;
		currentStep = 0;
	}

	@Override
	public void cancel() {
		Thread.currentThread().interrupt();
	}

	/**
	 * Complex network querying using PSICQUIC
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		interactionResults.clear();

		// Retrieve user input
		final Organism refOrg = qw.getSelectedRefOrganism();
		final List<String> proteinOfInterest = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		final List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		final List<Organism> otherOrgs = qw.getSelectedOrganisms();
		otherOrgs.remove(refOrg);

		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);

		System.out.println();
		
		UniProtProteinCollection proteinCollection = new UniProtProteinCollection();

		/*
		 * ----------------------------------------------- 
		 * PART ONE: search interaction in reference organism
		 * -----------------------------------------------
		 */
		monitor.setTitle("PSIQUIC interaction query in reference organism");
		List<BinaryInteraction> referenceInteractions = new ArrayList<BinaryInteraction>();
		final Collection<EncoreInteraction> clusterInteraction = new ArrayList<EncoreInteraction>();
		{
			// Search interaction of protein of interest in reference organism
			changeStep("Searching interaction for protein of interest", monitor);
			{
				// TODO verify protein id
				List<String> queries = new ArrayList<String>();
				for (String uniProtId : proteinOfInterest) {
					if (!UniprotId.isValid(uniProtId))
						throw new Exception(uniProtId + " is not a valid Uniprot ID.");

					MiQLExpressionBuilder query = new MiQLExpressionBuilder();
					MiQLExpressionBuilder org = new MiQLExpressionBuilder() {{
						add(new MiQLParameterBuilder("taxidA", refOrg.getTaxId()));
						addCondition(Operator.AND, new MiQLParameterBuilder("taxidB", refOrg.getTaxId()));
					}};
					query.addCondition(Operator.AND, new MiQLParameterBuilder("id", uniProtId.trim()));
					
					queries.add(query.toString());
				}
				referenceInteractions.addAll(client.getByQueries(queries));
			}
			System.out.println("interactions: " + referenceInteractions.size());

			// TODO stop here if no interaction was found

			// Filter non uniprot protein interaction
			referenceInteractions = (List<BinaryInteraction>) InteractionsUtil.filterNonUniprotAndNonRefOrg(referenceInteractions, refOrg.getTaxId());

			// Add secondary interactions
			changeStep("Searching secondary interactions...", monitor);
			{
				// Find interactors list (without protein of interest)
				HashSet<String> interactors = new HashSet<String>(InteractionsUtil.getInteractorsBinary(referenceInteractions, refOrg.getTaxId()));
				interactors.removeAll(proteinOfInterest);

				// Search interactions between interactors
				referenceInteractions.addAll(InteractionsUtil.getInteractionBetweenProtein(interactors, refOrg.getTaxId(),
						selectedDatabases));
			}

			// Remove duplicate interactions
			changeStep("Removing interaction duplicates...", monitor);
			{
				clusterInteraction.addAll(InteractionsUtil.clusterInteraction(referenceInteractions));
			}
		}

		// Get protein UniProt entries
		changeStep("Downloading UniProt protein entries...", monitor);
		HashSet<String> referenceInteractors;
		
		{
			// Get reference interactors
			referenceInteractors = new HashSet<String>(InteractionsUtil.getInteractorsEncore(clusterInteraction));
			referenceInteractors.addAll(proteinOfInterest);

			HashMap<String, UniProtProtein> uniProtProteins = UniProtEntryClient.getInstance().retrieveProteinsData(referenceInteractors);
			proteinCollection.addAll(new HashSet<UniProtProtein>(uniProtProteins.values()));

			System.out.println(referenceInteractors);
		}

		/*
		 * ----------------------------------------------- 
		 * PART TWO: search interaction in other organisms
		 * -----------------------------------------------
		 */
		monitor.setTitle("PSIQUIC interaction query in other organism(s)");
		InParanoidClient inParanoidClient = new InParanoidClient(9, 0.85);
		HashMap<Integer, Collection<EncoreInteraction>> orthologInteractionResults = new HashMap<Integer, Collection<EncoreInteraction>>();
		{
			final List<Integer> otherOrgsTaxIds = new ArrayList<Integer>();
			for (Organism org : otherOrgs) otherOrgsTaxIds.add(org.getTaxId());
			
			// Get orthologs of interactors
			changeStep("Find interactors orthologs...", monitor);
			final HashMap<String, HashMap<Integer, String>> orthologs = new HashMap<String, HashMap<Integer, String>>();
			{
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: " + proteinCollection.size());
				System.out.println("n# org: " + otherOrgsTaxIds.size());

				try {
					orthologs.putAll(inParanoidClient.searchOrthologForUniprotProtein(proteinCollection, otherOrgsTaxIds));
				} finally {}
			}

			// Get ortholog interactions
			changeStep("Find orthologs's interactions...", monitor);
			{
				// TODO maybe search orthologs interactions using thread
				// For each other organism
				for (final Organism org : otherOrgs) {
					HashSet<String> prots = new HashSet<String>() {
						{
							for (HashMap<Integer, String> ortho : orthologs.values()) {
								String id = ortho.get(org.getTaxId());
								if (id != null) add(id);
							}
						}
					};

					// Search all interactions for orthologs found in this organism
					Collection<EncoreInteraction> interactionBetweenOrthologs = InteractionsUtil.clusterInteraction(InteractionsUtil
							.getInteractionBetweenProtein(prots, org.getTaxId(), selectedDatabases));

					// Store interactions found for this organism
					orthologInteractionResults.put(org.getTaxId(), interactionBetweenOrthologs);
					System.out.println("ORG:" + org.getTaxId() + " -> " + prots.size() + " proteins found");
					System.out.println("ORG:" + org.getTaxId() + " -> " + interactionBetweenOrthologs.size() + " interactions found");

					// Get new interactors => not seen in reference organism
					{
						List<String> orthologInteractors = InteractionsUtil.getInteractorsEncore(interactionBetweenOrthologs);
						orthologInteractors.removeAll(prots);
						
						System.out.println("ORG:" + org.getTaxId() + " -> " + orthologInteractors.size() + " new protein found");
						
						if(!orthologInteractors.isEmpty()) {							
							HashMap<String, HashMap<Integer, String>> orthologsMultipleProtein = inParanoidClient.getOrthologsMultipleProtein(
									orthologInteractors, Arrays.asList(new Integer[] { refOrg.getTaxId() }));
							
							//Get UniProtProtein entry from reference organism
							for(HashMap<Integer, String> vals: orthologsMultipleProtein.values()) {
								String protInRefOrg = vals.get(org.getTaxId());
								
								if(proteinCollection.contains(protInRefOrg)) {
									UniProtProtein uniProtInRefOrg = UniProtEntryClient.getInstance().retrieveProteinData(protInRefOrg);
									proteinCollection.add(uniProtInRefOrg);
									inParanoidClient.searchOrthologForUniprotProtein(Arrays.asList(new UniProtProtein[]{uniProtInRefOrg}), otherOrgsTaxIds);
								}
							}
						}
					}
					System.out.println("--");
				}
			}
		}

		// Convert for network build
		referenceInteractions = InteractionsUtil.convertEncoreInteraction(clusterInteraction);
		interactionResults.addAll(referenceInteractions);
	}

	private void changeStep(String message, TaskMonitor monitor) {
		monitor.setStatusMessage(message);
		monitor.setProgress(++currentStep / NB_STEP);
	}

}

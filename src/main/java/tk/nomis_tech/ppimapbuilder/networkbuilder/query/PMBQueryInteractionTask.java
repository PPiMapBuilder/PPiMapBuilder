package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.networkbuilder.data.UniProtProtein;
import tk.nomis_tech.ppimapbuilder.orthology.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.InteractionsUtil;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;
import tk.nomis_tech.ppimapbuilder.util.ThreadedPsicquicSimpleClient;
import tk.nomis_tech.ppimapbuilder.util.UniProtEntryClient;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBQueryInteractionTask extends AbstractTask {

	private final Collection<BinaryInteraction> interactionResults;
	private QueryWindow qw;

	public PMBQueryInteractionTask(Collection<BinaryInteraction> interactionResults, QueryWindow qw) {
		this.interactionResults = interactionResults;
		this.qw = qw;
	}

	/**
	 * Complex network querying using PSICQUIC
	 */
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		interactionResults.clear();

		//Retrieve user input
		final Organism refOrg = qw.getSelectedRefOrganism();
		final List<String> proteinOfInterest = new ArrayList<String>(new HashSet<String>(qw.getSelectedUniprotID()));
		final List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		final List<Organism> otherOrgs = qw.getSelectedOrganisms();
		otherOrgs.remove(refOrg);

		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);

		//Init task monitor
		final double NB_STEP = 6.0;
		int step = 0;
		
		System.out.println();
		
		/* ----------------------------------------------- 
		 * PART ONE: search interaction in reference organism
		 * ----------------------------------------------- */
		monitor.setTitle("PSIQUIC interaction query in reference organism");
		List<BinaryInteraction> referenceInteractions = new ArrayList<BinaryInteraction>();
		final Collection<EncoreInteraction> clusterInteraction = new ArrayList<EncoreInteraction>();
		{
			//Search interaction of protein of interest in reference organism
			monitor.setStatusMessage("Searching interaction for protein of interest");
			monitor.setProgress(++step / NB_STEP);
			{
				//TODO verify protein id
				List<String> queries = new ArrayList<String>(); 
				for (String uniProtId : proteinOfInterest) {
					if (!UniprotId.isValid(uniProtId))
						throw new Exception(uniProtId + " is not a valid Uniprot ID.");
					
					queries.add(new StringBuilder("species:").append(refOrg.getTaxId()).append(" AND id:").append(uniProtId.trim()).toString());
				}
				referenceInteractions.addAll(client.getByQueries(queries));
			}
			System.out.println("interactions: "+referenceInteractions.size());
			
			//TODO stop here if no interaction was found
			
			// Filter non uniprot protein interaction
			referenceInteractions = (List<BinaryInteraction>) InteractionsUtil.filterNonUniprot(referenceInteractions);
			
			// Add secondary interactions
			monitor.setStatusMessage("Searching secondary interactions...");
			monitor.setProgress(++step / NB_STEP);
			{
				// Find interactors list (without protein of interest)
				HashSet<String> interactors = new HashSet<String>(InteractionsUtil.getInteractorsBinary(referenceInteractions));
				interactors.removeAll(proteinOfInterest);
				
				// Search interactions between interactors
				referenceInteractions.addAll(InteractionsUtil.getInteractionBetweenProtein(interactors, refOrg.getTaxId(),
						selectedDatabases));
			}
			
			// Remove duplicate interactions
			monitor.setStatusMessage("Removing interaction duplicates...");
			monitor.setProgress(++step / NB_STEP);
			{
				clusterInteraction.addAll(InteractionsUtil.clusterInteraction(referenceInteractions));
			}			
		}
		
		//Get protein UniProt entries
		monitor.setStatusMessage("Downloading UniProt protein entries...");
		monitor.setProgress(++step / NB_STEP);
		HashSet<String> referenceInteractors;
		HashSet<UniProtProtein> interactors; 
		{
			//Get reference interactors
			referenceInteractors = new HashSet<String>(InteractionsUtil.getInteractorsEncore(clusterInteraction));
			referenceInteractors.addAll(proteinOfInterest);
			
			HashMap<String, UniProtProtein> uniProtProteins = UniProtEntryClient.getInstance().retrieveProteinsData(referenceInteractors);
			interactors = new HashSet<UniProtProtein>(uniProtProteins.values());

			System.out.println(referenceInteractors);
		}

		/* ----------------------------------------------- 
		 * PART TWO: search interaction in other organisms 
		 * ----------------------------------------------- */
		monitor.setTitle("PSIQUIC interaction query in other organism(s)");
		{			
			//Get orthologs of interactors
			monitor.setStatusMessage("Find interactors orthologs...");
			monitor.setProgress(++step / NB_STEP);
			final HashMap<String,HashMap<Integer,String>> orthologs = new HashMap<String, HashMap<Integer,String>>();
			{
				final List<Integer> otherOrgsTaxIds = new ArrayList<Integer>(){{
					for(Organism org: otherOrgs) 
						add(org.getTaxId());
				}};
				System.out.println("--Search orthologs--");
				System.out.println("n# protein: "+interactors.size());
				System.out.println("n# org: "+otherOrgsTaxIds.size());
				
				try {
					InParanoidClient inParanoidClient = new InParanoidClient(9, 0.85);
					orthologs.putAll(inParanoidClient.searchOrthologForUniprotProtein(interactors, otherOrgsTaxIds));
				} finally{}
			}
			
			//Get ortholog interactions
			monitor.setStatusMessage("Find orthologs's interactions...");
			monitor.setProgress(++step / NB_STEP);
			HashMap<Integer, Collection<EncoreInteraction>> orthologInteractionResults = new HashMap<Integer, Collection<EncoreInteraction>>();
			{
				//TODO maybe search orthologs interactions using thread
				//For each other organism
				for(final Organism org: otherOrgs) {
					HashSet<String> prots = new HashSet<String>(){{
						for(HashMap<Integer,String> ortho: orthologs.values()){
							String id = ortho.get(org.getTaxId());
							if(id != null) add(id);
						}
					}};
										
					//Search all interactions for orthologs found in this organism
					Collection<EncoreInteraction> interactionBetweenOrthologs = InteractionsUtil.clusterInteraction(InteractionsUtil.getInteractionBetweenProtein(prots, org.getTaxId(), selectedDatabases));
					
					//Store interactions found for this organism
					orthologInteractionResults.put(org.getTaxId(), interactionBetweenOrthologs);
					System.out.println("ORG:" + org.getTaxId() + " -> " + prots.size()+" proteins found");
					System.out.println("ORG:" + org.getTaxId() + " -> " + interactionBetweenOrthologs.size()+" interactions found");
				}
			}			
		}
		
		//Convert for network build
		referenceInteractions = InteractionsUtil.convertEncoreInteraction(clusterInteraction);
		interactionResults.addAll(referenceInteractions);
	}

}

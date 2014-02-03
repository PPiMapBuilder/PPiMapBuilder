package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.orthology.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.InteractionsUtil;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;
import tk.nomis_tech.ppimapbuilder.util.ThreadedPsicquicSimpleClient;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

public class PMBQueryInteractionTask extends AbstractTask {

	private final Collection<BinaryInteraction> interactionResults;
	private QueryWindow qw;

	public PMBQueryInteractionTask(
			Collection<BinaryInteraction> interactionResults, QueryWindow qw) {
		this.interactionResults = interactionResults;
		this.qw = qw;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		interactionResults.clear();
		monitor.setTitle("PSIQUIC interaction query");
		
		Organism refOrg = qw.getSelectedRefOrganism();
		List<String> proteinOfInterest = qw.getSelectedUniprotID();
		List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		List<Organism> otherOrgs = qw.getSelectedOrganisms();
		
		List<BinaryInteraction> referenceInteractions = new ArrayList<BinaryInteraction>();

		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(selectedDatabases, 3);
		
		double max = proteinOfInterest.size() + 2.0;
		
		int step = 0;
		for (; step < proteinOfInterest.size(); step++) {
			String uniprotID = proteinOfInterest.get(step);
			
			monitor.setStatusMessage("Searching interaction of "+uniprotID+"...");
			monitor.setProgress((step+1.0)/max);
			
			if (!UniprotId.isValid(uniprotID))
				throw new Exception(uniprotID + " is not a valid Uniprot ID."); 
			
			/*String prot = InParanoidClient.getOrthologUniprotId(uniprotID, refOrg.getTaxId());
			if(prot == null) 
				throw new Exception(uniprotID + " not found in reference organism.");
			*/
			
			List<BinaryInteraction> interactions = client.getByQuery("species:"+refOrg.getTaxId()+" AND id:"+uniprotID);
			System.out.println(uniprotID+" interactions:"+interactions.size());
			
			//Search interactions
			referenceInteractions.addAll(interactions);
		}
		System.out.println("reference interactions: "+referenceInteractions.size());
		
		//Filter non uniprot protein interaction
		referenceInteractions = (List<BinaryInteraction>) InteractionsUtil.filterNonUniprot(referenceInteractions);
		System.out.println("after filtering "+referenceInteractions.size());
		
		//Find interactors list (without protein of interest)
		List<String> interactors = InteractionsUtil.getInteractorsBinary(referenceInteractions);
		interactors.removeAll(proteinOfInterest);
		
		//Add secondary interactions
		monitor.setStatusMessage("Searching secondary interactions...");
		monitor.setProgress(((++step)+1.0)/max);
		referenceInteractions.addAll(InteractionsUtil.getInteractionBetweenProtein(new HashSet<String>(interactors), refOrg.getTaxId(), selectedDatabases));
		
		System.out.println("interactions before cluster " + referenceInteractions.size());
		
		//Remove duplicate interactions
		monitor.setStatusMessage("Clustering interactions...");
		monitor.setProgress(((++step)+1.0)/max);
		Collection<EncoreInteraction> clusterInteraction = InteractionsUtil.clusterInteraction(referenceInteractions);
		
		System.out.println("interactions after cluster " + clusterInteraction.size());
		referenceInteractions = InteractionsUtil.convertEncoreInteraction(clusterInteraction);
		System.out.println("interactions after convert " + referenceInteractions.size());
		interactionResults.addAll(referenceInteractions);
	}

}

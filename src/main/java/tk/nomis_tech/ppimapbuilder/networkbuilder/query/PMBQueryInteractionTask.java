package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.orthology.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.orthology.Ortholog;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import tk.nomis_tech.ppimapbuilder.util.Organism;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

public class PMBQueryInteractionTask extends AbstractTask {

	private final Collection<BinaryInteraction> interactionResults;
	private QueryWindow qw;

	public PMBQueryInteractionTask(
			Collection<BinaryInteraction> interactionResults, QueryWindow qw) {
		this.interactionResults = interactionResults;
		this.qw = qw;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		interactionResults.clear();
		
		// QUERY WINDOW SELECTION
		// UniProt ID
		String uniprotID = qw.getSelectedUniprotID(); 
		if (!UniprotId.isValid(uniprotID)) { throw new Exception(uniprotID + " is not a valid Uniprot ID.");}// TODO: preload query to display number of result for the given id
		// Databases
		List<PsicquicService> selectedDatabases = qw.getSelectedDatabases(); 
		// Reference organism
		Organism org = qw.getSelectedRefOrganism();
		// Other organisms
		List<Organism> selectedOrganisms = qw.getSelectedOrganisms();

		// Retrieve uniprot IDs and their corresponding tax IDs
		LinkedHashMap<String, Integer> uniprotIDs = new LinkedHashMap<String, Integer>();
		uniprotIDs.put(uniprotID, org.getTaxId());
		if (!selectedOrganisms.isEmpty()) {
			for (Organism og : selectedOrganisms) {
				String orthoProtID = InParanoidClient.getOrthologUniprotId(uniprotID, og.getTaxId());
				if (orthoProtID != null) 
					uniprotIDs.put(orthoProtID, og.getTaxId());
			}
			System.out.println(uniprotIDs);
		}
		
		
//		StringBuilder listTaxID = new StringBuilder();
//		listTaxID.append("(" + org.getTaxId());
//
//		if (!selectedOrganisms.isEmpty()) {
//			listTaxID.append(" OR ");
//
//			for (Organism og : selectedOrganisms) {
//				listTaxID.append(String.valueOf(og.getTaxId()));
//				if (!(selectedOrganisms.indexOf(og) == selectedOrganisms.size() - 1))
//					listTaxID.append(" OR ");
//			}
//		}
//		listTaxID.append(")");
		
		// LAUNCH QUERY FOR EACH DATABASE
		for (PsicquicService service : selectedDatabases) {
			try {
				
				System.out.println("[INFO] : Database -> " + service.getName());
				PsicquicSimpleClient client = new PsicquicSimpleClient(
						service.getRestUrl());
				
				for (String uniID : uniprotIDs.keySet()) {
					try {
						System.out.println("[INFO] : Uniprot ID -> "+uniID+" | "+" Tax ID -> "+uniprotIDs.get(uniID));
						
						PsimiTabReader mitabReader = new PsimiTabReader();
						InputStream result = client.getByQuery("id:" + uniID
								+ " AND species:" + uniprotIDs.get(uniID),
								PsicquicSimpleClient.MITAB25);
						interactionResults.addAll(mitabReader.read(result));
		
						System.out.println("[INFO]\tInteractions found: "+ interactionResults.size());
					} catch (PsimiTabException t) {
						System.err.println("Interaction query failed on: "
								+ service.getName()+ "with the uniprot ID "+uniID);
					}
				}
			}
			catch (IOException t) {
				System.err.println("Interaction query failed on: "
						+ service.getName());
			}
		}

	}

}

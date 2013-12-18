package tk.nomis_tech.ppimapbuilder.networkbuilder.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
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
		List<PsicquicService> selectedDatabases = qw.getSelectedDatabases();
		
		//For the uniprotID
		String uniprotID = qw.getSelectedUniprotID();

		if (!uniprotID.matches("^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])$")) {
			throw new Exception(uniprotID + " is not a valid Uniprot ID.");	
			// TODO: preload query to display number of result for the given id
		}

		//For the reference Organism and the other organisms
		Organism org = qw.getSelectedRefOrganism();
		List<Organism> selectedOrganisms = qw.getSelectedOrganisms();
		
		StringBuilder listTaxID = new StringBuilder(); 
		listTaxID.append("(" + org.getTaxId() );
		
		if (!selectedOrganisms.isEmpty())
		{
			listTaxID.append(" OR ");
		
			for (Organism og : selectedOrganisms)
			{
				listTaxID.append(String.valueOf(og.getTaxId()));
				if(!(selectedOrganisms.indexOf(og) == selectedOrganisms.size()-1))
					listTaxID.append(" OR ");
		}
		listTaxID.append(")");
		for (PsicquicService service : selectedDatabases)
		{
				try {
					// System.out.println(service.toString());
					System.out.println("----- >>> " + service.getName()
						+ "----------------------");
					PsicquicSimpleClient client = new PsicquicSimpleClient(
						service.getRestUrl());
					PsimiTabReader mitabReader = new PsimiTabReader();
					InputStream result = client.getByQuery("id:"+uniprotID+" AND species:"+listTaxID,
						PsicquicSimpleClient.MITAB25);
				
					
					// if(binaryInteractions == null) binaryInteractions = mitabReader
					// .read(result);
					/* else */
					interactionResults.addAll(mitabReader.read(result));
	
					System.out.println("Interactions found: " + interactionResults.size());
					System.out.println("---------------------------------------");
				} catch (IOException t) {
					System.err.println("Interaction query failed on: " + service.getName());
				} catch (PsimiTabException t) {
					System.err.println("Interaction query failed on: " + service.getName());
				}
			}
	}

}

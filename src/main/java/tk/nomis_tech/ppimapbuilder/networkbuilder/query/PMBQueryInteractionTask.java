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
		Organism org = qw.getSelectedRefOrganism();

		for (PsicquicService service : selectedDatabases) {
			try {
				// System.out.println(service.toString());
				System.out.println("----- >>> " + service.getName()
					+ "----------------------");
				PsicquicSimpleClient client = new PsicquicSimpleClient(
					service.getRestUrl());
				PsimiTabReader mitabReader = new PsimiTabReader();
				InputStream result = client.getByQuery("id:P04040 AND species:"+org.getTaxId(),
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

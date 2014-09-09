package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import org.cytoscape.work.Task;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;

public class PMBQueryInteractionTaskTest {

	HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg = new HashMap<Organism, Collection<EncoreInteraction>>();
	UniProtEntrySet interactorPool = new UniProtEntrySet();
	UniProtEntrySet proteinOfInterestPool = new UniProtEntrySet();

	Organism human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
	Organism mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);

	QueryWindow qw = new QueryWindow() {
		@Override
		public Organism getSelectedRefOrganism() {
			return mouse;
		}

		@Override
		public List<String> getSelectedUniprotID() {
			return Arrays.asList("Q8VI75", "Q3THG9", "P04040");
		}

		@Override
		public List<PsicquicService> getSelectedDatabases() {
			return Arrays.asList(
				new PsicquicService("IntAct", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
				, new PsicquicService("BioGrid", null, "http://tyersrest.tyerslab.com:8805/psicquic/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
				, new PsicquicService("BIND", null, "http://webservice.baderlab.org:8480/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
				, new PsicquicService("DIP", null, "http://imex.mbi.ucla.edu/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
				, new PsicquicService("MINT", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/mint/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			);
		}

		@Override
		public List<Organism> getSelectedOrganisms() {
			return UserOrganismRepository.getInstance().getOrganisms();
		}
	};

	@Test
	public void test() throws Exception {
		Task t = new PMBQueryInteractionTask(interactionsByOrg, interactorPool, proteinOfInterestPool, qw);
		t.run(null);

		System.out.println("\n");

		for (Organism organism : interactionsByOrg.keySet()) {
			System.out.println("ORG: "+organism+"  -> "+ interactionsByOrg.get(organism).size() + " interactions");
		}

		System.out.println(proteinOfInterestPool.size() + " POIs");
		for (UniProtEntry uniProtEntry : proteinOfInterestPool) {
			System.out.print(uniProtEntry.getUniProtId() + "-");
		}
		System.out.println();


		System.out.println(interactorPool.size() + " interactors");
		for (UniProtEntry uniProtEntry : interactorPool) {
			System.out.print(uniProtEntry.getUniProtId() + "-");
		}
		System.out.println();
	}

}
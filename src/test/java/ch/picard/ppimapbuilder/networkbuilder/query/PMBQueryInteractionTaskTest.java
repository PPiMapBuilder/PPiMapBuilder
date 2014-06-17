package ch.picard.ppimapbuilder.networkbuilder.query;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
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

	Organism human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
	Organism mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);

	QueryWindow qw = new QueryWindow() {
		@Override
		public Organism getSelectedRefOrganism() {
			return human;
		}

		@Override
		public ArrayList<String> getSelectedUniprotID() {
			return new ArrayList<String>(Arrays.asList("Q3THG9"));
		}

		@Override
		public List<PsicquicService> getSelectedDatabases() {
			return Arrays.asList(
				new PsicquicService("IntAct", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			);
		}

		@Override
		public List<Organism> getSelectedOrganisms() {
			return Arrays.asList(mouse);
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
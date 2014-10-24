package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.util.test.*;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Arrays;
import java.util.Collection;

public class PMBInteractionNetworkBuildTaskFactoryTest {

	Organism human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
	Organism mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);

	NetworkQueryParameters nqp = new DummyNetworkQueryParameters(
			Arrays.asList(
					new PsicquicService("IntAct", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
					//, new PsicquicService("BioGrid", null, "http://tyersrest.tyerslab.com:8805/psicquic/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
					//, new PsicquicService("BIND", null, "http://webservice.baderlab.org:8480/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
					//, new PsicquicService("DIP", null, "http://imex.mbi.ucla.edu/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
					//, new PsicquicService("MINT", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/mint/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			),
			Arrays.asList(
					//"P04040"
					"Q8VI75",
					"B2RQC6"
			),
			human,
			Arrays.asList(
					InParanoidOrganismRepository.getInstance().getOrganismByTaxId(559292)
			)
	);

	@Test
	public void test() throws Exception {
		PMBInteractionNetworkBuildTaskFactory networkBuild = new PMBInteractionNetworkBuildTaskFactory(
				new DummyCyNetworkManager(), new DummyCyNetworkNaming(), new DummyCyNetworkFactory(),
				new DummyCyNetworkViewFactory(), new DummyCyNetworkViewManager(), new DummyCyLayoutAlgorithmManager(),
				new DummyVisualMappingManager(), nqp
		);

		TaskIterator taskIterator = networkBuild.createTaskIterator();
		while (taskIterator.hasNext()) {
			taskIterator.next().run(new DummyTaskMonitor());
		}

		System.out.println("\n");

		System.out.println(networkBuild.getProteinOfInterestPool().size() + " POIs");
		System.out.println(networkBuild.getInteractorPool().size() + " interactors");

		int nbInteraction = 0;
		for (Organism organism : networkBuild.getInteractionsByOrg().keySet()) {
			Collection<EncoreInteraction> encoreInteractions = networkBuild.getInteractionsByOrg().get(organism);
			nbInteraction += encoreInteractions.size();
			System.out.println("ORG: " + organism + "  -> " + networkBuild.getInteractionsByOrg().get(organism).size() + " interactions");

		}

		System.out.println(nbInteraction + " interactions");
	}

}
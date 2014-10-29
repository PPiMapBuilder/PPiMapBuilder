package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.util.test.*;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class PMBInteractionNetworkBuildTaskFactoryTest {

	static Organism human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
	static List<Organism> otherOrganisms = new ArrayList<Organism>() {{
		add(InParanoidOrganismRepository.getInstance().getOrganismByTaxId(7227));
		add(InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090));
		add(InParanoidOrganismRepository.getInstance().getOrganismByTaxId(559292));
		add(InParanoidOrganismRepository.getInstance().getOrganismByTaxId(6239));
	}};

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
					"B2RQC6",
					"O75153"
			),
			human,
			otherOrganisms
	);

	@Test
	public void test() throws Exception {
		DummyCyNetworkFactory networkFactory;

		PMBInteractionNetworkBuildTaskFactory networkBuild = new PMBInteractionNetworkBuildTaskFactory(
				new DummyCyNetworkManager(), new DummyCyNetworkNaming(), networkFactory = new DummyCyNetworkFactory(),
				new DummyCyNetworkViewFactory(), new DummyCyNetworkViewManager(), new DummyCyLayoutAlgorithmManager(),
				new DummyVisualMappingManager(), nqp
		);

		// Execute the whole network generation process
		TaskIterator taskIterator = networkBuild.createTaskIterator();
		while (taskIterator.hasNext()) {
			taskIterator.next().run(new DummyTaskMonitor());
		}

		{ // Display interactor count, interaction count and protein of interest count
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

		{ // Display unlinked nodes
			DummyCyNetwork network = networkFactory.getNetworks().get(0);
			List<CyEdge> edges = network.getEdgeList();
			Set<CyNode> nodes = new HashSet<CyNode>(network.getNodeList());
			Set<CyNode> linkedNodes = new HashSet<CyNode>();
			for (CyEdge edge : edges)
				for (CyNode node : nodes)
					if (edge.getSource() == node || edge.getTarget() == node)
						linkedNodes.add(node);

			System.out.println("\nEdges: "+ edges.size());
			System.out.println("Nodes: "+ nodes.size());
			System.out.println("Linked nodes: "+ linkedNodes.size());
			System.out.println("Unlinked nodes:");
			nodes.removeAll(linkedNodes);
			System.out.println(nodes);
		}
	}

}
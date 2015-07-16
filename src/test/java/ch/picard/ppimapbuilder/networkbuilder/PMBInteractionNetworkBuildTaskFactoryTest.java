/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */

package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.util.test.DummyCyLayoutAlgorithmManager;
import ch.picard.ppimapbuilder.util.test.DummyCyNetwork;
import ch.picard.ppimapbuilder.util.test.DummyCyNetworkFactory;
import ch.picard.ppimapbuilder.util.test.DummyCyNetworkManager;
import ch.picard.ppimapbuilder.util.test.DummyCyNetworkNaming;
import ch.picard.ppimapbuilder.util.test.DummyCyNetworkViewFactory;
import ch.picard.ppimapbuilder.util.test.DummyCyNetworkViewManager;
import ch.picard.ppimapbuilder.util.test.DummyCyNode;
import ch.picard.ppimapbuilder.util.test.DummyNetworkQueryParameters;
import ch.picard.ppimapbuilder.util.test.DummyTaskMonitor;
import ch.picard.ppimapbuilder.util.test.DummyVisualMappingManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskIterator;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PMBInteractionNetworkBuildTaskFactoryTest {

	static Organism human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
	static Organism elegans;
	static List<Organism> otherOrganisms = ImmutableList.of(
			InParanoidOrganismRepository.getInstance().getOrganismByTaxId(7227)//,
			//InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090),
			//InParanoidOrganismRepository.getInstance().getOrganismByTaxId(559292),
			//elegans = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(6239)
	);
	static List<PsicquicService> services = ImmutableList.of(
			//new PsicquicService("SPIKE", null, "http://spike.cs.tau.ac.il/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Lists.newArrayList(""))
			new PsicquicService("IntAct", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			//new PsicquicService("BioGrid", null, "http://tyersrest.tyerslab.com:8805/psicquic/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			//, new PsicquicService("BIND", null, "http://webservice.baderlab.org:8480/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			//, new PsicquicService("DIP", null, "http://imex.mbi.ucla.edu/psicquic-ws/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
			//, new PsicquicService("MINT", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/mint/webservices/current/search/", "true", "1", "", "", "true", Arrays.asList(""))
	);

	NetworkQueryParameters proteinNetworkParameters = new DummyNetworkQueryParameters(
			services,
			ImmutableList.of(
					//"P08238"//,
					"P04040"//,
					//"Q8VI75",
					//"B2RQC6",
					//"O75153"
					//"P61106"
					//"P14672",
					//"Q8VI75",
					//"B2RQC6",
					//"O75153",
					//"O75153",
					//"P04040",
					//"Q9EPL8",
					//"P62258",
					//"Q8VI75",
					//"B2RQC6"
			),
			human,
			otherOrganisms,
			false
	);
	NetworkQueryParameters interactomeNetworkParameters = new DummyNetworkQueryParameters(
			services,
			null,
			elegans,
			null,
			true
	);

	@Test
	public void testProteinNetwork() throws Exception {
		DummyCyNetworkFactory networkFactory = new DummyCyNetworkFactory();

		PMBInteractionNetworkBuildTaskFactory networkBuild = new PMBInteractionNetworkBuildTaskFactory(
				new DummyCyNetworkManager(), new DummyCyNetworkNaming(), networkFactory,
				new DummyCyNetworkViewFactory(), new DummyCyNetworkViewManager(), new DummyCyLayoutAlgorithmManager(),
				new DummyVisualMappingManager(), proteinNetworkParameters
		);

		// Execute the whole network generation process
		runNetworkQueryTask(networkBuild);

		// Display interactor count, interaction count and protein of interest count
		displayNodeInteractionCount(networkBuild);

		// Display unlinked nodes
		assertNodesConnectedToPOIs(networkFactory, networkBuild);
	}

	@Test
	public void testInteractomeNetwork() throws Exception {
		DummyCyNetworkFactory networkFactory = new DummyCyNetworkFactory();

		PMBInteractionNetworkBuildTaskFactory networkBuild = new PMBInteractionNetworkBuildTaskFactory(
				new DummyCyNetworkManager(), new DummyCyNetworkNaming(), networkFactory,
				new DummyCyNetworkViewFactory(), new DummyCyNetworkViewManager(), new DummyCyLayoutAlgorithmManager(),
				new DummyVisualMappingManager(), interactomeNetworkParameters
		);

		// Execute the whole network generation process
		runNetworkQueryTask(networkBuild);

		// Display interactor count, interaction count and protein of interest count
		displayNodeInteractionCount(networkBuild);
	}

	private void displayNodeInteractionCount(PMBInteractionNetworkBuildTaskFactory networkBuild) {
		System.out.println();
		int nbInteraction = 0;
		for (Organism organism : networkBuild.getInteractionsByOrg().keySet()) {
			Collection<EncoreInteraction> encoreInteractions = networkBuild.getInteractionsByOrg().get(organism);
			nbInteraction += encoreInteractions.size();
			System.out.println("ORG: " + organism.getScientificName() + "  -> " + networkBuild.getInteractionsByOrg().get(organism).size() + " interactions");
		}
		System.out.println(networkBuild.getProteinOfInterestPool().size() + " POIs");
		System.out.println(networkBuild.getInteractorPool().size() + " interactors");
		System.out.println(nbInteraction + " interactions");
	}

	private void runNetworkQueryTask(PMBInteractionNetworkBuildTaskFactory networkBuild) throws Exception {
		TaskIterator taskIterator = networkBuild.createTaskIterator();
		final DummyTaskMonitor taskMonitor = new DummyTaskMonitor(false);
		while (taskIterator.hasNext()) {
			taskIterator.next().run(taskMonitor);
		}
	}

	private static void assertNodesConnectedToPOIs(DummyCyNetworkFactory networkFactory, PMBInteractionNetworkBuildTaskFactory networkBuild) {
		DummyCyNetwork network = networkFactory.getNetworks().get(0);
		List<CyEdge> edges = network.getEdgeList();
		Set<CyNode> nodes = ImmutableSet.copyOf(network.getNodeList());
		Set<CyNode> POIsNodes = Sets.newHashSet();

		for (CyNode node : nodes) {
			final String name = ((DummyCyNode) node).getName();
			for (UniProtEntry entry : networkBuild.getProteinOfInterestPool()) {
				if (entry.getAccessions().contains(name)) {
					POIsNodes.add(node);
					break;
				}
			}
		}

		Set<CyNode> nodesLinkedToPOIs = Sets.newHashSet();
		nodesLinkedToPOIs.addAll(POIsNodes);
		for (CyEdge edge : edges) {
			final DummyCyNode target = (DummyCyNode) edge.getTarget();
			final DummyCyNode source = (DummyCyNode) edge.getSource();
			if (source != target) {
				if (POIsNodes.contains(source))
					nodesLinkedToPOIs.add(target);
				else if (POIsNodes.contains(target))
					nodesLinkedToPOIs.add(source);
			}
		}

		System.out.println();
		System.out.println("Edges: " + edges.size());
		System.out.println("Nodes: " + nodes.size());
		System.out.println("Nodes linked to at least one of the POIs: " + nodesLinkedToPOIs.size());

		// Get nodes not linked to POIs
		Set<CyNode> unlinkedNodes = Sets.newHashSet(nodes);
		unlinkedNodes.removeAll(nodesLinkedToPOIs);
		System.out.print("Unlinked nodes: ");
		System.out.println(unlinkedNodes);
		System.out.print("POI nodes: ");
		System.out.println(POIsNodes);

		// Assert network contains no unliked nodes
		Assert.assertEquals(0, unlinkedNodes.size());
	}

}
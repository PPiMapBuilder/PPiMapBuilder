package ch.picard.ppimapbuilder.data.ontology.client.web;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.OntologyTerm;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class QuickGOClientTest {

	@Test
	public void testGetSlimmedTermList() throws Exception {
		final Organism saccharomyces = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(559292);

		final Protein Q12018 = new Protein("Q12018", saccharomyces); //CDC53
		final Protein P07269 = new Protein("P07269", saccharomyces); //PHO2
		final Protein P24031 = new Protein("P24031", saccharomyces); //PHO3
		final Protein P07270 = new Protein("P07270", saccharomyces); //PHO4

		final GeneOntologyTerm GO0003677 = new GeneOntologyTerm("GO:0003677", "DNA binding", 'F');
		final GeneOntologyTerm GO0001071 = new GeneOntologyTerm("GO:0001071", "nucleic acid binding transcription factor activity", 'F');
		final GeneOntologyTerm GO0030674 = new GeneOntologyTerm("GO:0030674", "protein binding, bridging", 'F');
		final GeneOntologyTerm GO0016791 = new GeneOntologyTerm("GO:0016791", "phosphatase activity", 'F');

		QuickGOClient client = new QuickGOClient();

		Set<OntologyTerm> terms = new HashSet<OntologyTerm>();
		terms.addAll(Arrays.asList(GO0003677, GO0001071, GO0030674, GO0016791));

		Set<Protein> proteins = new HashSet<Protein>();
		proteins.addAll(Arrays.asList(Q12018, P07269, P24031, P07270));

		HashMap<Protein, Set<GeneOntologyTerm>> expected = new HashMap<Protein, Set<GeneOntologyTerm>>(){{
			put(Q12018, new HashSet<GeneOntologyTerm>(){{addAll(Arrays.asList(GO0003677, GO0030674));}});
			put(P07269, new HashSet<GeneOntologyTerm>(){{addAll(Arrays.asList(GO0003677, GO0001071));}});
			put(P24031, new HashSet<GeneOntologyTerm>(){{addAll(Arrays.asList(GO0016791));}});
			put(P07270, new HashSet<GeneOntologyTerm>(){{addAll(Arrays.asList(GO0003677, GO0001071));}});
		}};

		HashMap<Protein, Set<GeneOntologyTerm>> actual = client.getSlimmedTermList(terms, proteins);

		Assert.assertEquals(expected, actual);
	}
}
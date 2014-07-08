package ch.picard.ppimapbuilder.data.ontology.client.web;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class QuickGOClientTest {

	private static final Organism saccharomyces = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(559292);

	private static final Protein Q12018 = new Protein("Q12018", saccharomyces); //CDC53
	private static final Protein P07269 = new Protein("P07269", saccharomyces); //PHO2
	private static final Protein P24031 = new Protein("P24031", saccharomyces); //PHO3
	private static final Protein P07270 = new Protein("P07270", saccharomyces); //PHO4

	private static final GeneOntologyTerm GO0003677 = new GeneOntologyTerm("GO:0003677", "DNA binding", 'F');
	private static final GeneOntologyTerm GO0001071 = new GeneOntologyTerm("GO:0001071", "nucleic acid binding transcription factor activity", 'F');
	private static final GeneOntologyTerm GO0030674 = new GeneOntologyTerm("GO:0030674", "protein binding, bridging", 'F');
	private static final GeneOntologyTerm GO0016791 = new GeneOntologyTerm("GO:0016791", "phosphatase activity", 'F');

	private static final Set<GeneOntologyTerm> terms = new HashSet<GeneOntologyTerm>();

	private static final Set<Protein> proteins = new HashSet<Protein>();

	static {
		terms.addAll(Arrays.asList(GO0003677, GO0001071, GO0030674, GO0016791));

		proteins.addAll(Arrays.asList(Q12018, P07269, P24031, P07270));
	}

	@Test
	public void testGetSlimmedTermList() throws Exception {
		QuickGOClient.GOSlimClient client = new QuickGOClient.GOSlimClient();

		HashMap<Protein, Set<GeneOntologyTerm>> expected = new HashMap<Protein, Set<GeneOntologyTerm>>() {{
			put(Q12018, new HashSet<GeneOntologyTerm>() {{
				addAll(Arrays.asList(GO0003677, GO0030674));
			}});
			put(P07269, new HashSet<GeneOntologyTerm>() {{
				addAll(Arrays.asList(GO0003677, GO0001071));
			}});
			put(P24031, new HashSet<GeneOntologyTerm>() {{
				addAll(Arrays.asList(GO0016791));
			}});
			put(P07270, new HashSet<GeneOntologyTerm>() {{
				addAll(Arrays.asList(GO0003677, GO0001071));
			}});
		}};

		HashMap<Protein, Set<GeneOntologyTerm>> actual = client.searchProteinGOSlim(terms, proteins);

		Assert.assertEquals(expected, actual);
	}


	@Test
	public void testGenerateRequests() throws URISyntaxException {
		final int MIN = 100;
		final int MAX = 5000;
		final int STEP = 100;

		for(int i = MIN; i <= MAX; i += STEP) {
			int numberOfGOTerms = (MAX + MIN) - i;
			int numberOfProteins = i;

			final Set<GeneOntologyTerm> terms = new HashSet<GeneOntologyTerm>();
			for (int j = 0; j < numberOfGOTerms; j++)
				terms.add(new GeneOntologyTerm("GO" + j, "", 'F'));

			final Set<Protein> proteins = new HashSet<Protein>();
			for (int j = 0; j < numberOfProteins; j++)
				proteins.add(new Protein("P" + j, saccharomyces));

			for (URI uri : QuickGOClient.GOSlimClient.generateRequests(new ArrayList<GeneOntologyTerm>(terms), new ArrayList<Protein>(proteins))) {
				int length = uri.toString().length();

				//System.out.println(length);
				System.out.println(uri);
				Assert.assertTrue(length < QuickGOClient.MAX_URL_LENGTH);
			}
		}
	}
}
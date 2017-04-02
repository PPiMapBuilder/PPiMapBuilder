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
    
package ch.picard.ppimapbuilder.data.ontology.client.web;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.test.DummyTaskMonitor;
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
		QuickGOClient.GOSlimClient client = new QuickGOClient.GOSlimClient(
			new ExecutorServiceManager()
		);

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

		HashMap<Protein, Set<GeneOntologyTerm>> actual = client.searchProteinGOSlim(terms, proteins, new DummyTaskMonitor());

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
				//System.out.println(uri);
				Assert.assertTrue(length < QuickGOClient.MAX_URL_LENGTH);
			}
		}
	}
}
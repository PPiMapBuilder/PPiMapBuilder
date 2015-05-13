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
    
package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.TestUtils;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PMBProteinOrthologCacheClientTest {
	//Test organisms
	private static Organism mouse;
	private static Organism human;
	private static Organism gallus;

	//Test proteins
	private static Protein P04040;
	private static Protein P24270;
	private static Protein F1NGJ7;

	private static Protein P10144;
	private static Protein P04187;
	private static Protein H9L027;

	private static Double minimumScore;

	private static File testFolderOutput;

	private static PMBProteinOrthologCacheClient cache;
	private ThreadedProteinOrthologClientDecorator threadedCache;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(PMBProteinOrthologCacheClientTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);

		minimumScore = 0.95;

		human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		P10144 = new Protein("P10144", human);
		P04187 = new Protein("P04187", mouse);
		H9L027 = new Protein("H9L027", gallus);

		cache = PMBProteinOrthologCacheClient.getInstance();
	}

	@Test
	public void testAddGet() throws Exception {
		List<? extends Protein> expected, actual;

		//Add othology relation between catalase protein in human and mouse
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(P24270, 1d)
		));

		//Get the ortholog from human catalase in mouse
		expected = Arrays.asList(P24270);
		actual = cache.getOrtholog(P04040, P24270.getOrganism(), minimumScore);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from mouse catalase to human
		//Order doesn't matter so it should work same as above
		expected = Arrays.asList(P04040);
		actual = cache.getOrtholog(P24270, P04040.getOrganism(), minimumScore);
		Assert.assertEquals(expected, actual);


		//Add othology relation between catalase protein in human and cock
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(F1NGJ7, 1d)
		));

		//Get the ortholog from human catalase in cock
		expected = Arrays.asList(F1NGJ7);
		actual = cache.getOrtholog(P04040, F1NGJ7.getOrganism(), minimumScore);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in human
		expected = Arrays.asList(P04040);
		actual = cache.getOrtholog(F1NGJ7, P04040.getOrganism(), minimumScore);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in mouse
		//Shouldn't work as the orthology relation is not transitive
		expected = new ArrayList<Protein>();
		actual = cache.getOrtholog(F1NGJ7, P24270.getOrganism(), minimumScore);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testThreadedGet() throws Exception {
		cache.addOrthologGroup(
				new OrthologGroup(
						new OrthologScoredProtein(P10144, 1d),
						new OrthologScoredProtein(P04187, 1d)
				)
		);
		cache.addOrthologGroup(
				new OrthologGroup(
						new OrthologScoredProtein(P10144, 1d),
						new OrthologScoredProtein(H9L027, 1d)
				)
		);

		for(int nbThread = 1 ; nbThread <= 4; nbThread++) {
			threadedCache = new ThreadedProteinOrthologClientDecorator(cache, new ExecutorServiceManager(3));

			Map<Organism, List<OrthologScoredProtein>> expected = new HashMap<Organism, List<OrthologScoredProtein>>();
			expected.put(mouse, cache.getOrtholog(P10144, mouse, minimumScore));
			expected.put(gallus, cache.getOrtholog(P10144, gallus, minimumScore));

			Map<Organism, List<OrthologScoredProtein>> actual =
					threadedCache.getOrthologsMultiOrganism(P10144, Arrays.asList(mouse, gallus), minimumScore);

			Assert.assertEquals(expected, actual);
		}
	}

}

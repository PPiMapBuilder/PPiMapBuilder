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
    
package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.picard.ppimapbuilder.TestUtils;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ProteinOrthologWebCachedClientTest {

	private static Organism human;
	private static Organism mouse;
	private static Organism gallus;
	private static Protein P04040;
	private static Protein F1NGJ7;
	private static Protein P24270;
	private static ProteinOrthologWebCachedClient client;
	private static File output;
	private static Double MINIMUM_ORTHOLOGY_SCORE;

	@BeforeClass
	public static void before() throws Exception {
		output = TestUtils.createTestOutputFolder(ProteinOrthologWebCachedClientTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(output);

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = UserOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		PMBProteinOrthologCacheClient cacheClient = PMBProteinOrthologCacheClient.getInstance();
		InParanoidClient webClient = new InParanoidClient();
		client = new ProteinOrthologWebCachedClient(webClient, cacheClient);

		MINIMUM_ORTHOLOGY_SCORE = 0.95;
	}

	@Test
	public void testGetOrthologNotCached() throws Exception {
		List<? extends Protein> expected, actual;

		expected = Arrays.asList(P24270);
		actual = client.getOrtholog(P04040, mouse, MINIMUM_ORTHOLOGY_SCORE);
		Assert.assertEquals(expected, actual);

		expected = Arrays.asList(P24270);
		actual = client.getOrtholog(F1NGJ7, mouse, MINIMUM_ORTHOLOGY_SCORE);
		Assert.assertEquals(expected, actual);

		expected = Arrays.asList(P04040);
		actual = client.getOrtholog(F1NGJ7, human, MINIMUM_ORTHOLOGY_SCORE);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologCached() throws Exception {
		// Just rerunning previous test now cached
		testGetOrthologNotCached();
	}
}

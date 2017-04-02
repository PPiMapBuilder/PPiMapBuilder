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
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SpeciesPairProteinOrthologCacheTest {

	//Test organisms
	private static Organism mouse;
	private static Organism human;

	//Test proteins
	private static Protein P04040;
	private static Protein Q06141;
	private static Protein Q58A65;
	private static Protein P35230;
	private static Protein O60271;
	private static Protein P24270;

	private static SpeciesPairProteinOrthologCache cache;

	private static File testFolderOutput;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(SpeciesPairProteinOrthologCacheTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);

		human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);

		P04040 = new Protein("P04040", human);
		Q06141 = new Protein("Q06141", human);
		O60271 = new Protein("O60271", human);
		P35230 = new Protein("P35230", mouse);
		Q58A65 = new Protein("Q58A65", mouse);
		P24270 = new Protein("P24270", mouse);

		cache = new SpeciesPairProteinOrthologCache(human, mouse) {{
			addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(Q06141, 1d),
				new OrthologScoredProtein(P35230, 1d)
			));
			addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(Q58A65, 1d),
				new OrthologScoredProtein(O60271, 1d)
			));
		}};
	}

	@Test
	public void testAddOrtholog() throws Exception {
		List<? extends Protein> expected, actual;

		expected = Arrays.asList(P24270);
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(P24270, 1d)
		));
		actual = cache.getOrtholog(P04040, mouse, 1d);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologFail() throws Exception {
		List<? extends Protein> actual;

		actual = cache.getOrtholog(new Protein("DSF", null), mouse, 0d);
		Assert.assertTrue(actual.isEmpty());
	}

	@Test
	public void testGetOrthologSuccess() throws Exception {
		List<? extends Protein> expected, actual;

		expected = Arrays.asList(Q06141);
		actual = cache.getOrtholog(P35230, human, 1d);
		Assert.assertEquals(expected, actual);

		expected = Arrays.asList(Q58A65);
		actual = cache.getOrtholog(O60271, mouse, 1d);
		Assert.assertEquals(expected, actual);
	}

}

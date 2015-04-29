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
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PMBProteinOrthologCacheClientLoadedTest {
	//Test organisms
	private Organism mouse;
	private Organism human;
	private PMBProteinOrthologCacheClient cache;
	private SpeciesPairProteinOrthologCache speciesPairProteinOrthologCache;

	@Before
	public void before() throws IOException {
		File folder = TestUtils.getTestOutputFolder("InParanoidCacheLoaderTaskTest-output-1400425389240");
		System.out.println(folder.getAbsolutePath());
		PMBSettings.getInstance().setOrthologCacheFolder(folder);

		human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);

		cache = PMBProteinOrthologCacheClient.getInstance();
		speciesPairProteinOrthologCache = cache.getSpeciesPairProteinOrthologCache(human, mouse);
	}

	@Test
	public void testGet2() throws Exception {
		System.out.println(
			speciesPairProteinOrthologCache.getOrtholog(new Protein("Q8WZ42", human), mouse, 1d)
		);
	}

	@Test
	public void testGet() throws Exception {
		System.out.println(
				speciesPairProteinOrthologCache.getOrtholog(new Protein("Q96LM9", human), mouse, 1d)
		);
	}

	@Test
	public void testUserPercent() throws IOException {
		System.err.println("---");
		double userPercent = cache.getPercentLoadedFromOrganisms(UserOrganismRepository.getInstance().getOrganisms());
		System.err.println("user: "+userPercent);
	}

	@Test
	public void testInpPercent() throws IOException {
		System.err.println("---");
		double inpPercent = cache.getPercentLoadedFromOrganisms(InParanoidOrganismRepository.getInstance().getOrganisms());
		System.err.println("inp: "+inpPercent);
	}

}

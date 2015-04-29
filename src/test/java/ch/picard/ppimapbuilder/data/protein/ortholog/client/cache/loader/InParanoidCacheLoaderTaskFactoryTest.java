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
    
package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.picard.ppimapbuilder.TestUtils;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InParanoidCacheLoaderTaskFactoryTest {
	private static File testFolderOutput;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(InParanoidCacheLoaderTaskFactoryTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);
	}

	@Test
	public void testLoadInCache() throws Exception {
		List<Organism> organisms = Arrays.asList(
				UserOrganismRepository.getInstance().getOrganismByTaxId(9606),
				UserOrganismRepository.getInstance().getOrganismByTaxId(10090),
				UserOrganismRepository.getInstance().getOrganismByTaxId(284812),
				UserOrganismRepository.getInstance().getOrganismByTaxId(559292),
				UserOrganismRepository.getInstance().getOrganismByTaxId(36329)
		);

		//organisms = UserOrganismRepository.getInstance().getOrganisms();
		//organisms = InParanoidOrganismRepository.getInstance().getOrganisms().subList(10, 60);

		InParanoidCacheLoaderTaskFactory loader = new InParanoidCacheLoaderTaskFactory();
		loader.setOrganisms(organisms);
		loader.createTaskIterator().next().run(null);
	}
}

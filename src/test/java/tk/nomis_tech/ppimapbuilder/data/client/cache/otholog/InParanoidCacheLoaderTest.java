package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InParanoidCacheLoaderTest {
	private static File testFolderOutput;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutPutFolder(InParanoidCacheLoaderTest.class.getSimpleName(), false);
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);
	}


	@Test
	public void testLoadInCache() throws Exception {
		List<Organism> organisms = Arrays.asList(
				OrganismRepository.getInstance().getOrganismByTaxId(9606),
				OrganismRepository.getInstance().getOrganismByTaxId(10090)
		);

		InParanoidCacheLoader loader = new InParanoidCacheLoader(organisms);
		loader.run(null);
	}
}

package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InParanoidCacheLoaderTaskTest {
	private static File testFolderOutput;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(InParanoidCacheLoaderTaskTest.class.getSimpleName());
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

		List<Organism> organisms1 = InParanoidOrganismRepository.getInstance().getOrganisms();

		//InParanoidCacheLoaderTask loader = new InParanoidCacheLoaderTask(UserOrganismRepository.getInstance().getOrganisms());
		//InParanoidCacheLoaderTask loader = new InParanoidCacheLoaderTask(organisms);
		InParanoidCacheLoaderTask loader = new InParanoidCacheLoaderTask(organisms1.subList(10, 60));
		loader.run(null);
	}
}

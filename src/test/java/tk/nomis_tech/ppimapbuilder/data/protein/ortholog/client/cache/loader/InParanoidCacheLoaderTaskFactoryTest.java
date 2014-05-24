package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

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

		InParanoidCacheLoaderTaskFactory loader = new InParanoidCacheLoaderTaskFactory(organisms);
		loader.createTaskIterator().next().run(null);
	}
}

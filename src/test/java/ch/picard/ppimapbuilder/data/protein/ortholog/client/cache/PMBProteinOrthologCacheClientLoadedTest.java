package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import org.junit.Before;
import org.junit.Test;
import ch.picard.ppimapbuilder.TestUtils;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;

public class PMBProteinOrthologCacheClientLoadedTest {
	//Test organisms
	private Organism mouse;
	private Organism human;
	private PMBProteinOrthologCacheClient cache;
	private ThreadedProteinOrthologClientDecorator<PMBProteinOrthologCacheClient> threadedCache;
	private SpeciesPairProteinOrthologCache speciesPairProteinOrthologCache;

	@Before
	public void before() throws IOException {
		File folder = TestUtils.getTestOutputFolder("InParanoidCacheLoaderTaskTest-output-1400425389240");
		System.out.println(folder.getAbsolutePath());
		PMBSettings.getInstance().setOrthologCacheFolder(folder);

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);

		cache = PMBProteinOrthologCacheClient.getInstance();
		threadedCache = new ThreadedProteinOrthologClientDecorator<PMBProteinOrthologCacheClient>(cache);
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

package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;

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
	private static ThreadedProteinOrthologClientDecorator threadedCache;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(PMBProteinOrthologCacheClientTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);

		minimumScore = 0.95;

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = UserOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		P10144 = new Protein("P10144", human);
		P04187 = new Protein("P04187", mouse);
		H9L027 = new Protein("H9L027", gallus);

		cache = PMBProteinOrthologCacheClient.getInstance();
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
		cache.addOrthologGroup(
				new OrthologGroup(
						new OrthologScoredProtein(P04187, 1d),
						new OrthologScoredProtein(H9L027, 1d)
				)
		);

		threadedCache = new ThreadedProteinOrthologClientDecorator(cache);
	}

	@Test
	public void testAdd() throws Exception {
		Protein expected;
		Protein actual;

		//Add othology relation between catalase protein in human and mouse
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(P24270, 1d)
		));

		//Get the ortholog from human catalase in mouse
		expected = P24270;
		actual = cache.getOrtholog(P04040, P24270.getOrganism(), 1d);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from mouse catalase to human
		//Order doesn't matter so it should work same as above
		expected = P04040;
		actual = cache.getOrtholog(P24270, P04040.getOrganism(), 1d);
		Assert.assertEquals(expected, actual);


		//Add othology relation between catalase protein in human and cock
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(F1NGJ7, 1d)
		));

		//Get the ortholog from human catalase in cock
		expected = F1NGJ7;
		actual = cache.getOrtholog(P04040, F1NGJ7.getOrganism(), 1d);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in human
		expected = P04040;
		actual = cache.getOrtholog(F1NGJ7, P04040.getOrganism(), 1d);
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in mouse
		//Shouldn't work as the orthology relation is not transitive
		expected = null;
		actual = cache.getOrtholog(F1NGJ7, P24270.getOrganism(), 1d);
		Assert.assertEquals(expected, actual);
	}

}

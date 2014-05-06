package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProteinOrthologCacheClientTest {
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


	private static File testFolderOutput;

	private static ProteinOrthologCacheClient cache;
	private static ProteinOrthologCacheClient cacheEmpty;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutPutFolder(ProteinOrthologCacheClientTest.class.getSimpleName(), true);
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = UserOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		P10144 = new Protein("P10144", human);
		P04187 = new Protein("P04187", mouse);
		H9L027 = new Protein("H9L027", gallus);

		cache = ProteinOrthologCacheClient.getInstance();
		cache.addOrthologGroup(P10144, P04187);
		cache.addOrthologGroup(P10144, H9L027);
		cache.addOrthologGroup(P04187, H9L027);
	}

	@Test
	public void testAdd() throws IOException {
		Protein expected;
		Protein actual;

		//Add othology relation between catalase protein in human and mouse
		cache.addOrthologGroup(P04040, P24270);

		//Get the ortholog from human catalase in mouse
		expected = P24270;
		actual = cache.getOrtholog(P04040, P24270.getOrganism());
		Assert.assertEquals(expected, actual);

		//Get the ortholog from mouse catalase to human
		//Order doesn't matter so it should work same as above
		expected = P04040;
		actual = cache.getOrtholog(P24270, P04040.getOrganism());
		Assert.assertEquals(expected, actual);


		//Add othology relation between catalase protein in human and cock
		cache.addOrthologGroup(F1NGJ7, P04040);

		//Get the ortholog from human catalase in cock
		expected = F1NGJ7;
		actual = cache.getOrtholog(P04040, F1NGJ7.getOrganism());
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in human
		expected = P04040;
		actual = cache.getOrtholog(F1NGJ7, P04040.getOrganism());
		Assert.assertEquals(expected, actual);

		//Get the ortholog from cock catalase in mouse
		//Shouldn't work as the orthology relation is not transitive
		expected = null;
		actual = cache.getOrtholog(F1NGJ7, P24270.getOrganism());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologMultiOrganism() throws IOException {
		Map<Organism, Protein> expected = new HashMap<Organism, Protein>() {{
			put(mouse, P24270);
			put(gallus, F1NGJ7);
		}};
		Map<Organism, Protein> actual = cache.getOrthologsMultiOrganism(P04040, Arrays.asList(mouse, gallus));
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologMultiOrganismMultiProtein() throws IOException {
		Map<Protein, Map<Organism, Protein>> expected = new HashMap<Protein, Map<Organism, Protein>>() {{
			put(P04040, new HashMap<Organism, Protein>() {{
				put(mouse, P24270);
				put(gallus, F1NGJ7);
			}});

			put(P10144, new HashMap<Organism, Protein>() {{
				put(mouse, P04187);
				put(gallus, H9L027);
			}});
		}};
		Map<Protein, Map<Organism, Protein>> actual = cache.getOrthologsMultiOrganismMultiProtein(Arrays.asList(P04040, P10144), Arrays.asList(mouse, gallus));
		Assert.assertEquals(expected, actual);
	}
}

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
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;

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

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);

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
		Protein expected = P24270;
		cache.addOrthologGroup(new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(P24270, 1d)
		));
		Protein actual = cache.getOrtholog(P04040, mouse, 1d);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologFail() throws Exception {
		Protein actual;

		actual = cache.getOrtholog(new Protein("DSF", null), mouse, 0d);
		Assert.assertNull(actual);
	}

	@Test
	public void testGetOrthologSuccess() throws Exception {
		Protein expected;
		Protein actual;

		expected = Q06141;
		actual = cache.getOrtholog(P35230, human, 1d);
		Assert.assertEquals(expected, actual);

		expected = Q58A65;
		actual = cache.getOrtholog(O60271, mouse, 1d);
		Assert.assertEquals(expected, actual);
	}

}

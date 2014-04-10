package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.io.IOException;

public class OrganismPairOrthologCacheTest {

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

	private static OrganismPairOrthologCache cache;
	private static OrganismPairOrthologCache cacheEmpty;

	@BeforeClass
	public static void init() throws IOException {
		human = OrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = OrganismRepository.getInstance().getOrganismByTaxId(10090);

		P04040 = new Protein("P04040", human);
		Q06141 = new Protein("Q06141", human);
		O60271 = new Protein("O60271", human);
		P35230 = new Protein("P35230", mouse);
		Q58A65 = new Protein("Q58A65", mouse);
		P24270 = new Protein("P24270", mouse);


		cacheEmpty = new OrganismPairOrthologCache(human, mouse);
		cache = new OrganismPairOrthologCache(human, mouse) {{
			addOrthologGroup(Q06141, P35230);
			addOrthologGroup(Q58A65, O60271);
		}};
		//TODO: change ortholog cache folder specially for JUnit tests
	}

	@Test
	public void testAddOrtholog() throws Exception {
		Protein expected = P24270;
		cache.addOrthologGroup(P04040, expected);
		Protein actual = cache.getOrtholog(P04040, mouse);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologFail() throws Exception {
		Protein actual = cacheEmpty.getOrtholog(P04040, mouse);
		Assert.assertNull(actual);
	}

	@Test
	public void testGetOrthologSuccess() throws Exception {
		Protein expected;
		Protein actual;

		expected = Q06141;
		actual = cache.getOrtholog(P35230, human);
		Assert.assertEquals(expected, actual);

		expected = null;
		actual = cache.getOrtholog(P35230, mouse);
		Assert.assertEquals(expected, actual);

		expected = Q58A65;
		actual = cache.getOrtholog(O60271, mouse);
		Assert.assertEquals(expected, actual);
	}
}

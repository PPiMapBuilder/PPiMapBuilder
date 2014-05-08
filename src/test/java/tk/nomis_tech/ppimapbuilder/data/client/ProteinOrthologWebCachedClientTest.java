package tk.nomis_tech.ppimapbuilder.data.client;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.ProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.client.web.ortholog.InParanoidClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;

public class ProteinOrthologWebCachedClientTest {

	private static Organism human;
	private static Organism mouse;
	private static Organism gallus;
	private static Protein P04040;
	private static Protein F1NGJ7;
	private static Protein P24270;
	private static ProteinOrthologWebCachedClient client;
	private static File output;

	@BeforeClass
	public static void before() throws Exception {

		output = TestUtils.createTestOutPutFolder();
		PMBSettings.getInstance().setOrthologCacheFolder(output);

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = UserOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		ProteinOrthologCacheClient cacheClient = ProteinOrthologCacheClient.getInstance();
		InParanoidClient webClient = new InParanoidClient(0.95);
		client = new ProteinOrthologWebCachedClient();
		client.setCacheClient(cacheClient);
		client.setWebClient(webClient);
	}

	@AfterClass
	public static void after() throws Exception {
		TestUtils.recursiveDelete(output);
	}

	@Test
	public void testGetOrthologNotCached() throws Exception {
		Protein expected;
		Protein actual;

		expected = P24270;
		actual = client.getOrtholog(P04040, mouse);
		Assert.assertEquals(expected, actual);

		expected = P24270;
		actual = client.getOrtholog(F1NGJ7, mouse);
		Assert.assertEquals(expected, actual);

		expected = P04040;
		actual = client.getOrtholog(F1NGJ7, human);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologCached() throws Exception {
		// Just rerunning previous test now cached
		testGetOrthologNotCached();
	}
}

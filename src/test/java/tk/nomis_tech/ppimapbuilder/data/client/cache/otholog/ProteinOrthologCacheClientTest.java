package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;

public class ProteinOrthologCacheClientTest {
	//Test organisms
	private static Organism mouse;
	private static Organism human;
	private static Organism gallus;

	//Test proteins
	private static Protein P04040;
	private static Protein P24270;
	private static Protein F1NGJ7;


	private static File testFolderOutput;

	private static ProteinOrthologCacheClient cache;

	@BeforeClass
	public static void before() throws IOException {
		testFolderOutput = TestUtils.createTestOutPutFolder();
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);

		human = OrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = OrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = OrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		cache = ProteinOrthologCacheClient.getInstance();
	}

	@Test
	public void testGetFail() throws IOException {
		Protein actual = cache.getOrtholog(P04040, mouse);
		Assert.assertNull(actual);
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

	@AfterClass
	public static void after() throws IOException {
		TestUtils.recursiveDelete(testFolderOutput);
	}
}

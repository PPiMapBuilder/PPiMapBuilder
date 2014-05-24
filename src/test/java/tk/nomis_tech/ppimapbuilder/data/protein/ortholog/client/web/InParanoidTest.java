package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.web;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.TestUtils;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InParanoidTest {

	private static Organism human;
	private static Organism mouse;
	private static Organism gallus;
	private static Protein P04040;
	private static Protein F1NGJ7;
	private static Protein P24270;
	private static List<Protein> humanProts;
	private static ThreadedInParanoidClient client;
	private static File testFolderOutput;
	private static Double MINIMUM_ORTHOLOGY_SCORE;

	@BeforeClass
	public static void init() throws IOException {
		testFolderOutput = TestUtils.createTestOutputFolder(InParanoidTest.class.getSimpleName());
		PMBSettings.getInstance().setOrthologCacheFolder(testFolderOutput);
		client = new ThreadedInParanoidClient(3);
		MINIMUM_ORTHOLOGY_SCORE = 0.85;

		human = UserOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = UserOrganismRepository.getInstance().getOrganismByTaxId(10090);
		gallus = UserOrganismRepository.getInstance().getOrganismByTaxId(9031);

		P04040 = new Protein("P04040", human);
		P24270 = new Protein("P24270", mouse);
		F1NGJ7 = new Protein("F1NGJ7", gallus);

		humanProts = new ArrayList<Protein>() {{
			for (String id : Arrays.asList("Q9UBL6", "O68769", "P24409", "Q13867" ,"P30154", "Q14145", "P31944", "Q5NF37", "P04544",
					"A2AL36", "P61106", "Q77M19", "P62993", "Q02413", "Q15075", "P35293", "O43747", "Q92878", "P01040", "P05089", "Q2T9J0",
					"P40337", "Q0IIN1", "P61254", "P60900", "Q6XUX3", "Q70EK8", "Q92692", "P30480", "Q81QG7", "P25311", "Q8CZX0", "Q8D092",
					"A6NMY6", "Q14164", "P04040", "O14964", "P51610", "Q9NRH1", "Q9H0R8", "Q06124", "P20340", "Q15051", "Q08188", "Q8ZIG9",
					"P15336", "P05067", "Q01968", "Q12933", "Q14315", "P48200")
			) {
				add(new Protein(id, human));
			}
		}};
	}

	@Test
	public void testGetOrtholog() throws Exception {
		Protein expected = P24270;
		Protein actual = client.getOrtholog(P04040, mouse, MINIMUM_ORTHOLOGY_SCORE);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologsMultiOrganisms() throws Exception {
		Map<Organism, Protein> expected = new HashMap<Organism, Protein>() {{
			put(mouse, P24270);
			put(gallus, F1NGJ7);
		}};
		Map<Organism, OrthologScoredProtein> actual = client.getOrthologsMultiOrganism(P04040, Arrays.asList(mouse, gallus), MINIMUM_ORTHOLOGY_SCORE);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrthologsMultiOrganismMultiProtein2Thread() throws Exception {
		Map<Protein, Map<Organism, OrthologScoredProtein>> actual = client.getOrthologsMultiOrganismMultiProtein(humanProts, Arrays.asList(mouse, gallus), MINIMUM_ORTHOLOGY_SCORE);
		System.out.println(actual);
	}

	/*
	@Test
	public void testGetOrthologsMultiOrganismMultiProtein3Thread() throws IOException {
		client.emptyFileCache();
		InParanoidClient client = new InParanoidClient(3, 0.85);
		Map<Protein, Map<Organism, Protein>> actual = client.getOrthologsMultiOrganismMultiProtein(humanProts, Arrays.asList(mouse, gallus));
		System.out.println(actual);
	}

	@Test
	public void testGetOrthologsMultiOrganismMultiProtein4Thread() throws IOException {
		client.emptyFileCache();
		InParanoidClient client = new InParanoidClient(4, 0.85);
		Map<Protein, Map<Organism, Protein>> actual = client.getOrthologsMultiOrganismMultiProtein(humanProts, Arrays.asList(mouse, gallus));
		System.out.println(actual);
	}

	//@Test
	public void getOrthologsSingleProtein() throws IOException {
		System.out.println("Get orthologs of "+ids.size()+" proteins without threads: ");
		HashMap<String, HashMap<Integer, String>> orthologsProteins = new HashMap<String, HashMap<Integer, String>>();

		for (String id : ids) {
			orthologsProteins.put(id, client.getOrthologsSingleProtein(id, taxIds));
		}
		System.out.println(orthologsProteins);
	}

	@Test
	public void getOrthologsMultipleProtein() throws Exception {
		System.out.println("Get organism of "+ids.size()+" proteins with threads: ");
		HashMap<String, HashMap<Integer, String>> orthologsProteins = client.getOrthologsMultipleProtein(ids, taxIds);
		System.out.print(orthologsProteins);
	}*/
}

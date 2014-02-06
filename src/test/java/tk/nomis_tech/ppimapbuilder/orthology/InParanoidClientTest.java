package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import tk.nomis_tech.ppimapbuilder.networkbuilder.data.UniProtProtein;

public class InParanoidClientTest {

	private static List<Integer> taxIds;
	private static List<String> ids;

	@BeforeClass
	public static void init() {
		taxIds = new ArrayList<Integer>() {
			{
				add(9031); // Gallus gallus
				add(9606); // Homo sapiens
				add(3702); // Arabidopsis thaliana
				add(6239); // Caenorhabditis elegans
				add(7227); // Drosophila Melanogaster
				add(10090); // Mus musculus
				add(4932); // Saccharomyces cerevisiae
				add(4896); // Schizosaccharomyces pombe
			}
		};
		ids = Arrays.asList(new String[] { "Q9UBL6", "O68769", "P24409", "Q13867", "P30154", "Q14145", "P31944", "Q5NF37", "P04544",
				"A2AL36", "P61106", "Q77M19", "P62993", "Q02413", "Q15075", "P35293", "O43747", "Q92878", "P01040", "P05089", "Q2T9J0",
				"P40337", "Q0IIN1", "P61254", "P60900", "Q6XUX3", "Q70EK8", "Q92692", "P30480", "Q81QG7", "P25311", "Q8CZX0", "Q8D092",
				"A6NMY6", "Q14164", "P04040", "O14964", "P51610", "Q9NRH1", "Q9H0R8", "Q06124", "P20340", "Q15051", "Q08188", "Q8ZIG9",
				"P15336", "P05067", "Q01968", "Q12933", "Q14315", "P48200" });
	}

	@Test
	public void test0() throws IOException {
		HashMap<Integer, String> orthologs = InParanoidClient.getInstance().getOrthologs("P07900", taxIds);
		System.out.println(orthologs);
	}

	@Test
	public void test1() throws IOException {
		System.out.print("\ntest1: ");
		HashMap<String, HashMap<Integer, String>> orthologsProteins = new HashMap<String, HashMap<Integer, String>>();

		for (String id : ids) {
			HashMap<Integer, String> orthologForOrg = new HashMap<Integer, String>();

			for (Integer taxId : taxIds) {
				try {
					String result = InParanoidClient.getInstance().getOrtholog(id, taxId);
					if (result != null)
						orthologForOrg.put(taxId, result);
				} catch (Exception e) {
				}

			}
			orthologsProteins.put(id, orthologForOrg);

		}
		System.out.println(orthologsProteins);
	}

	@Test
	public void test2() throws Exception {
		System.out.print("\ntest2: ");
		HashMap<String, HashMap<Integer, String>> orthologsProteins = InParanoidClient.getInstance().getOrthologsProteins(ids, taxIds);
		System.out.print(orthologsProteins);
	}

	/**
	 * Test searchOrthologForUniprotProtein
	 */
	@Test
	public void test3() throws IOException {
		List<UniProtProtein> prots = new ArrayList<UniProtProtein>() {
			{
				add(new UniProtProtein("P04040", null, 9606, null, true));
			}
		};

		InParanoidClient.getInstance().searchOrthologForUniprotProtein(prots, taxIds);
		// assertTrue(actual == expected);
	}

}

package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;

@SuppressWarnings("serial")
public class InParanoidClientTest {

	private static List<Integer> taxIds;
	private static List<String> ids;
	private static List<UniProtEntry> prots;
	private static InParanoidClient client;

	@BeforeClass
	public static void init() {
		client = new InParanoidClient(9, 0.85);
		taxIds = Arrays.asList(new Integer[] { 
				9031, // Gallus gallus
				9606, // Homo sapiens
				//3702, // Arabidopsis thaliana
				6239, // Caenorhabditis elegans
				7227, // Drosophila Melanogaster
				10090,// Mus musculus
				4932, // Saccharomyces cerevisiae
				//4896, // Schizosaccharomyces pombe
		});
		ids = Arrays.asList(new String[] { "Q9UBL6", "O68769", "P24409", "Q13867", "P30154", "Q14145", "P31944", "Q5NF37", "P04544",
				"A2AL36", "P61106", "Q77M19", "P62993", "Q02413", "Q15075", "P35293", "O43747", "Q92878", "P01040", "P05089", "Q2T9J0",
				"P40337", "Q0IIN1", "P61254", "P60900", "Q6XUX3", "Q70EK8", "Q92692", "P30480", "Q81QG7", "P25311", "Q8CZX0", "Q8D092",
				"A6NMY6", "Q14164", "P04040", "O14964", "P51610", "Q9NRH1", /*"Q9H0R8", "Q06124", "P20340", "Q15051", "Q08188", "Q8ZIG9",
				"P15336", "P05067", "Q01968", "Q12933", "Q14315", "P48200"*/ });
		ids = Arrays.asList(new String[] {"Q2T9J0", "P05067", "Q9H0R8", "Q06124", "P61106", "Q14145", "P04040", "P20340", "Q70EK8"});
		prots = new ArrayList<UniProtEntry>(){{
			for(String id: ids)
				add(new UniProtEntry(id, null, null, 9606, null, true));
		}};
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
		System.out.println("Get ortholog of "+ids.size()+" proteins with threads: ");
		HashMap<String, HashMap<Integer, String>> orthologsProteins = client.getOrthologsMultipleProtein(ids, taxIds);
		System.out.print(orthologsProteins);
	}

	//@Test
	public void searchOrthologForUniprotProtein() throws IOException {
		client.searchOrthologForUniprotProtein(prots, taxIds);
	}

}

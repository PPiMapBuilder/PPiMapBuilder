package ch.picard.ppimapbuilder.data.organism;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganismTest extends TestCase {

	/**
	 * Tests formatting of organism names
	 */
	@Test
	public void testFormat() {
		String expected;
		String actual;

		List<Organism> organisms = Arrays.asList(
				new Organism("homo sapiens", 9606),
				new Organism("HOMO SAPIENS", 9606)
		);
		for (Organism organism : organisms) {
			expected = "H.sapiens";
			actual = organism.getAbbrName();
			Assert.assertEquals(expected, actual);

			expected = "Homo";
			actual = organism.getGenus();
			Assert.assertEquals(expected, actual);

			expected = "sapiens";
			actual = organism.getSpecies();
			Assert.assertEquals(expected, actual);
		}

	}



	@Ignore
	@Test
	/**
	 * Using UniProt taxonomy to search organism data of the InParanoid taxonomy identifier list.
	 * Use only to generate code instantiation for all above organisms
	 */
	public void generateInparanoidOrganisms() throws IOException {
		String url = "http://www.uniprot.org/taxonomy/$id.rdf";

		String inParanoidTaxIDListUrl = "http://inparanoid.sbc.su.se/download/8.0_current/sequences/species.inparanoid8";
		URL u = new URL(inParanoidTaxIDListUrl);
		HttpURLConnection connection = (HttpURLConnection) u.openConnection();

		ArrayList<Integer> failed = new ArrayList<Integer>();

		if (connection.getResponseCode() == 200) {
			BufferedReader input;

			input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			Pattern compile = Pattern.compile("(\\d+)\\.fasta");

			String line = null;
			while ((line = input.readLine()) != null) {
				Matcher matcher = compile.matcher(line);

				if (matcher.find()) {
					Integer id = Integer.valueOf(matcher.group(1));
					//System.out.println(id);

					try {
						Connection c = Jsoup.connect(url.replace("$id", id.toString()))
								.ignoreContentType(true);

						Document document = c.get();

						String scientificName = document.select("scientificName").text();
						String mnemonic = document.select("mnemonic").text();
						String commonName = document.select("commonName").text();

						Organism org = new Organism(scientificName, mnemonic, commonName, id);

						System.out.println("new Organism(\"" + scientificName + "\", \"" + mnemonic + "\", \"" + commonName + "\", " + id + "),");

						/*System.out.println("[");
                        System.out.println("\t" + org.getScientificName());
						System.out.println("\t" + org.getGenus());
						System.out.println("\t" + org.getSpecies());
						System.out.println("\t" + org.getAbbrName());
						System.out.println("]");*/
					} catch (Exception e) {
						failed.add(id);
						//e.printStackTrace();
					}
				}
			}

			input.close();

		}

		System.out.println();
		System.out.println("Failed organisms: " + failed);
	}
}

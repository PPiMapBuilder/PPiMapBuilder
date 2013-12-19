package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Kevin Gravouil
 */
public class InParanoidClient {

	private static String url;
	private static HashMap<String, String> species = new HashMap<String, String>();

	private InParanoidClient() {
	}

	public static String get(String key) {
		return species.get(key);
	}

	public static boolean containsKey(String key) {
		return species.containsKey(key);
	}

	public static String put(String key, String value) {
		return species.put(key, value);
	}

	public static String remove(String key) {
		return species.remove(key);
	}

	public static void clear() {
		species.clear();
	}

	public static boolean containsValue(String value) {
		return species.containsValue(value);
	}

	public static Set<String> keySet() {
		return species.keySet();
	}

	public static void getOrthologUniprotId(String uniprotId, Integer orthologNcbiTaxId) {
		try {
			InParanoidClient.url = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi?"
				+ "idtype=proteinid;"
				+ "all_or_selection=selection;"
				+ "scorelimit=0.05;"
				+ "rettype=xml;"
				+ "id=" + uniprotId + ";"
				+ "specieslist=" + Ortholog.translateTaxID(orthologNcbiTaxId) + ";";

			Document doc = Jsoup.connect(InParanoidClient.url).get();

			for (Element e : doc.select("protein")) {
//				Integer tmp = ;

//				System.out.println("### " + tmp + " // given:" + orthologNcbiTaxId);
				if (Integer.valueOf(e.attr("speclink").replace("http://www.uniprot.org/taxonomy/", "")).equals(orthologNcbiTaxId)) {

					if (Double.valueOf(e.attr("score")) >= 0.85) { // TODO: review this to pick only the best one ?
//						System.out.println("SPECIE: " + e.attr("speclong"));
//						System.out.println("TAXID: " + e.attr("speclink").replace("http://www.uniprot.org/taxonomy/", ""));
						System.out.println("Orthologous protein id: " + e.attr("prot_id") + " (score: " + e.attr("score") + ")");
					}
				}
			}
		} catch (IOException ex) {
			System.out.println("No ortholgs found.");
		}
	}

	public static void testClient() {

		ArrayList<Integer> orthotaxids = new ArrayList<Integer>() {
			{
				add(10090);
				add(3702);
				add(6239);
			}
		};

		ArrayList<String> ids = new ArrayList() {
			{
				add("P07900");
				add("P04040");
				add("Q04565");
				add("Q9C519");
				add("P69891");
				add("P02091");
				add("P02008");
			}
		};
		for (Integer orthotaxid : orthotaxids) {
			System.out.println("--------[" + orthotaxid + "]-----------------------------------");
			// tests
			for (String id : ids) {
				System.out.println("==" + id);
				InParanoidClient.getOrthologUniprotId(id, orthotaxid);
			}

		}

	}
}

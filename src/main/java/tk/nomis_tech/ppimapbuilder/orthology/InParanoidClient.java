package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.UniProtProtein;

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

	public static String getOrthologUniprotId(String uniprotId, Integer orthologNcbiTaxId) {
		try {
			InParanoidClient.url = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi?"
				+ "idtype=proteinid;"
				+ "all_or_selection=selection;"
				+ "scorelimit=0.05;"
				+ "rettype=xml;"
				+ "id=" + uniprotId + ";"
				+ "specieslist=" + Ortholog.translateTaxID(orthologNcbiTaxId) + ";";

			//System.out.println("#Query :"+InParanoidClient.url);
			Document doc = Jsoup.connect(InParanoidClient.url).get();

			for (Element e : doc.select("protein")) {
//				Integer tmp = ;

//				System.out.println("### " + tmp + " // given:" + orthologNcbiTaxId);
				if (Integer.valueOf(e.attr("speclink").replace("http://www.uniprot.org/taxonomy/", "")).equals(orthologNcbiTaxId)) {

					if (Double.valueOf(e.attr("score")) >= 0.85) { // TODO: review this to pick only the best one ?
//						System.out.println("SPECIE: " + e.attr("speclong"));
//						System.out.println("TAXID: " + e.attr("speclink").replace("http://www.uniprot.org/taxonomy/", ""));
						System.out.println("Orthologous protein id: " + e.attr("prot_id") + " (score: " + e.attr("score") + ")");
					
						return e.attr("prot_id");
					}
				}
			}
		} catch (IOException ex) {
			System.out.println("No ortholgs found.");
			return null;
		}
		return null;
	}
	
	/**
	 * Search ortholog for an UniProtProtein
	 * @param prot the protein from which the ortholog will be retrieved
	 * @param taxid the taxonomy identifier of the organism in which the ortholog will be searched
	 */
	public static boolean searchOrthologForUniprotProtein(UniProtProtein prot, Integer taxid) {
		String orthologId = getOrthologUniprotId(prot.getUniprotId(), taxid);
		
		if(orthologId == null) return false;
		
		prot.addOrtholog(new OrthologProtein(orthologId, taxid));
		return true;
	}
}

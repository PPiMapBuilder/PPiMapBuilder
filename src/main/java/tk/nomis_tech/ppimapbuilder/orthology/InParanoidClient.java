package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Kevin Gravouil
 */
public class InParanoidClient {

	private String url;
	private String refID;
	private String orthoID;
	private HashMap<String, String> species = new HashMap<String, String>();

	public InParanoidClient() {
		this.testClient();
	}

	public String get(String key) {
		return species.get(key);
	}

	public boolean containsKey(String key) {
		return species.containsKey(key);
	}

	public String put(String key, String value) {
		return species.put(key, value);
	}

	public String remove(String key) {
		return species.remove(key);
	}

	public void clear() {
		species.clear();
	}

	public boolean containsValue(String value) {
		return species.containsValue(value);
	}

	public Set<String> keySet() {
		return species.keySet();
	}

	private void testClient() {
		
		System.out.println("");

		ArrayList<String> ids = new ArrayList();
		ids.add("P07900");
		ids.add("P04040");
		ids.add("Q04565");
		ids.add("Q9C519");
		ids.add("P69891");
		ids.add("P02091");
		ids.add("P02008");

		ArrayList<String> notfound = new ArrayList();

		for (String id : ids) {
			System.out.println("== " + id);
			try {
				this.url = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi?"
					+ "idtype=proteinid;"
					+ "all_or_selection=selection;"
					+ "scorelimit=0.05;"
					+ "rettype=xml;"
					+ "id=" + id + ";"
					+ "specieslist=255;";

				Document doc = Jsoup.connect(this.url).get();
				for (Element e : doc.select("protein")) {
					if (Double.valueOf(e.attr("score")) >= 0.95) {
						System.out.println("\tSPECIE:" + e.attr("speclong"));
						System.out.println("\tPTN_ID:" + e.attr("prot_id"));
						System.out.println("\tSCORE:" + e.attr("score"));
						System.out.println("");
					}
				}
			} catch (IOException ex) {
				notfound.add(id);
			}
		}

		if (!notfound.isEmpty()) {
			Logger.getLogger(InParanoidClient.class.getName()).log(Level.WARNING, "no ortholog found for {0}", notfound.toString());
		}
	}
}

package tk.nomis_tech.ppimapbuilder.data.client.web.protein;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tk.nomis_tech.ppimapbuilder.data.GeneOntologyCategory;
import tk.nomis_tech.ppimapbuilder.data.GeneOntologyModel;
import tk.nomis_tech.ppimapbuilder.data.client.AbstractThreadedClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple Java client for UniProt entry service
 */
public class UniProtEntryClient extends AbstractThreadedClient {

	private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

	public UniProtEntryClient(int nThread) {
		super(nThread);
	}

	/**
	 * Retrieve UniProt entry of a protein
	 */
	public UniProtEntry retrieveProteinData(String uniprotId) throws IOException {
		UniProtEntry protein = null;

		Document doc = null;
		final int MAX_TRY = 4;
		int pos = 0;
		IOException lastError = null;
		do {
			try {
				Connection connect = Jsoup.connect(new StringBuilder(UNIPROT_URL).append(uniprotId).append(".xml").toString());
				connect.timeout(6000);
				doc = connect.get();
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 404) return null;//No protein entry found
				lastError = new IOException(e);
			} catch (SocketTimeoutException e) {
				lastError = new IOException(e);
			}
		} while (doc == null && ++pos < MAX_TRY);
		if (doc == null) throw lastError;

		Organism organism = null;
		String geneName = null;
		ArrayList<String> synonymGeneNames = new ArrayList<String>();
		String proteinName = null;
		String ec_number = null;
		boolean reviewed = false;

		// ORGANISM
		for (Element e : doc.select("organism")) {
			organism = UserOrganismRepository.getInstance().getOrganismByTaxId(Integer.valueOf(e.select("dbReference").attr("id")));
			break;
		}

		// GENE NAME AND SYNONYMS
		for (Element e : doc.select("gene")) {
			for (Element f : e.select("name")) {
				if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
					geneName = f.text();
				} else { // Else, we organism the names as synonyms
					synonymGeneNames.add(f.text());
				}
			}
		}

		// PROTEIN NAME
		for (Element e : doc.select("protein")) {
			if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
				proteinName = e.select("recommendedName").select("fullName").text();
			} else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exist, we take the submitted name (usually from TrEMBL but not always)
				proteinName = e.select("submittedName").select("fullName").text();
			}
			break;
		}

		// REVIEWED
		for (Element e : doc.select("entry")) {
			reviewed = e.attr("dataset").equalsIgnoreCase("Swiss-Prot"); // If the protein comes from Swiss-Prot, it is reviewed
			break;
		}

		// EC NUMBER
		for (Element e : doc.select("ecNumber")) {
			ec_number = e.text();
			break;
		}

		// PROTEIN CREATION
		protein = new UniProtEntry(uniprotId, geneName, ec_number, organism, proteinName, reviewed);
		protein.setSynonymGeneNames(synonymGeneNames);

		// GENE ONTOLOGIES
		for (Element e : doc.select("dbReference")) {
			if (e.attr("type").equals("GO")) {
				String id = e.attr("id");
				GeneOntologyCategory category = null;
				String term = null;
				for (Element f : e.select("property")) {
					if (f.attr("type").equals("term")) {
						String value = f.attr("value");
						String[] values = value.split(":");
						if (values[0].equals("C")) {
							category = GeneOntologyCategory.CELLULAR_COMPONENT;
						} else if (values[0].equals("F")) {
							category = GeneOntologyCategory.MOLECULAR_FUNCTION;
						} else if (values[0].equals("P")) {
							category = GeneOntologyCategory.BIOLOGICAL_PROCESS;
						}
						term = values[1];
						break;
					}
				}
				GeneOntologyModel go = new GeneOntologyModel(id, term, category);
				if (category == GeneOntologyCategory.CELLULAR_COMPONENT) {
					protein.addCellularComponent(go);
				} else if (category == GeneOntologyCategory.BIOLOGICAL_PROCESS) {
					protein.addBiologicalProcess(go);
				} else if (category == GeneOntologyCategory.MOLECULAR_FUNCTION) {
					protein.addMolecularFunction(go);
				}
			}
		}

		System.out.println("uniprotEntryClient:"+protein.getUniProtId()+":"+pos+"try-ok");
		return protein;
	}

	/**
	 * Retrieves UniProt entry data of a list of protein using threaded execution pool
	 */
	public HashMap<String, UniProtEntry> retrieveProteinsData(Collection<String> uniProtIds) {
		final List<String> uniProtIdsArray = new ArrayList<String>(new HashSet<String>(uniProtIds));
		final List<Future<UniProtEntry>> requests = new ArrayList<Future<UniProtEntry>>();
		final CompletionService<UniProtEntry> completionService = new ExecutorCompletionService<UniProtEntry>(newFixedThreadPool());

		// For each protein => search UniProt entry
		for (final String uniProtId : uniProtIdsArray) {
			requests.add(completionService.submit(new Callable<UniProtEntry>() {
				@Override
				public UniProtEntry call() throws Exception {
					UniProtEntry result = null;

					final int MAX_TRY = 2;
					int i = 0;
					do {
						try {
							if (!uniProtId.equals(null))
								result = retrieveProteinData(uniProtId);
						} finally {
							i++;
						}
					} while (result == null || i > MAX_TRY);

					return result;
				}
			}));
		}

		// Collect all uniprot entries results
		final HashMap<String, UniProtEntry> results = new HashMap<String, UniProtEntry>();
		for (int i = 0; i < requests.size(); i++) {
			try {
				Future<UniProtEntry> take = completionService.take();
				UniProtEntry result = take.get();
				if (result != null)
					results.put(uniProtIdsArray.get(requests.indexOf(take)), result);
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				cause.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}
}

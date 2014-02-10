package tk.nomis_tech.ppimapbuilder.webservice;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tk.nomis_tech.ppimapbuilder.data.GOCategory;
import tk.nomis_tech.ppimapbuilder.data.GeneOntologyModel;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;

/**
 * Simple Java client for UniProt entry service
 */
public class UniProtEntryClient {
	private static final String uniprotUrl = "http://www.uniprot.org/uniprot/";
	private static final int NB_THREAD = 5;
	private static UniProtEntryClient _instance;

	private UniProtEntryClient() {}
	
	public static UniProtEntryClient getInstance() {
		if (_instance == null)
			_instance = new UniProtEntryClient();
		return _instance;
	}

	/**
	 * Retrieve UniProt entry of a protein 
	 */
	public UniProtEntry retrieveProteinData(String uniprotId) throws IOException {
		UniProtEntry prot = null;
		
		Document doc = null;
		final int MAX_TRY = 4;
		int pos = 0;
		IOException lastError = null;
		do{
			try {
				Connection connect = Jsoup.connect(new StringBuilder(uniprotUrl).append(uniprotId).append(".xml").toString());
				connect.timeout(6000);
				doc = connect.get();
			} catch (HttpStatusException e) {
				if(e.getStatusCode() == 404) return null;//No protein entry found
				lastError = new IOException(e);
			} catch(SocketTimeoutException e) {
				lastError = new IOException(e);
			}
		} while(++pos < MAX_TRY);
		if(doc == null) throw lastError;
		
		Integer taxId = null;
		String geneName = null;
		ArrayList<String> synonymGeneNames = new ArrayList<String>();
		String proteinName = null;
		String ec_number = null;
		boolean reviewed = false;
		
		// TAX ID
		for (Element e : doc.select("organism")) {
			taxId = Integer.valueOf(e.select("dbReference").attr("id")); // There is no problem, this is in the same way each time
			break;
		}
		
		// GENE NAME AND SYNONYMS
		for (Element e : doc.select("gene")) {
			for (Element f : e.select("name")) {
				if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
					geneName = f.text();
				}
				else { // Else, we store the names as synonyms
					synonymGeneNames.add(f.text());
				}
			}
		}
		
		// PROTEIN NAME
		for (Element e : doc.select("protein")) {
			if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
				proteinName = e.select("recommendedName").select("fullName").text();
			}
			else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exist, we take the submitted name (usually from TrEMBL but not always)
				proteinName = e.select("submittedName").select("fullName").text();
			}
			break;
		}
		
		// REVIEWED
		for (Element e : doc.select("entry")) {
			reviewed = e.attr("dataset").equalsIgnoreCase("Swiss-Prot")?true:false; // If the protein comes from Swiss-Prot, it is reviewed
			break;
		}
		
		// EC NUMBER
		for (Element e : doc.select("ecNumber")) {
			ec_number = e.text();
			break;
		}
		
		// PROTEIN CREATION
		prot = new UniProtEntry(uniprotId, geneName, ec_number, taxId, proteinName, reviewed);
		prot.setSynonymGeneNames(synonymGeneNames);
		
		// GENE ONTOLOGIES
		for (Element e : doc.select("dbReference")) {
			if (e.attr("type").equals("GO")) {
				String id = e.attr("id");
				GOCategory category = null;
				String term = null;
				for (Element f: e.select("property")) {
					if (f.attr("type").equals("term")) {
						String value = f.attr("value");
						String[] values = value.split(":");
						if (values[0].equals("C")) {
							category = GOCategory.CELLULAR_COMPONENT;
						}
						else if (values[0].equals("F")) {
							category = GOCategory.MOLECULAR_FUNCTION;
						}
						else if (values[0].equals("P")) {
							category = GOCategory.BIOLOGICAL_PROCESS;
						}
						term = values[1];
						break;
					}
				}
				GeneOntologyModel go = new GeneOntologyModel(id, term, category);
				if (category == GOCategory.CELLULAR_COMPONENT) {
					prot.addCellularComponent(go);
				}
				else if (category == GOCategory.BIOLOGICAL_PROCESS) {
					prot.addBiologicalProcess(go);
				}
				else if (category == GOCategory.MOLECULAR_FUNCTION) {
					prot.addMolecularFunction(go);
				}
			}
		}
		
		return prot;
	}

	/**
	 * Retrieves UniProt entry data of a list of protein using threaded execution pool
	 */
	public HashMap<String, UniProtEntry> retrieveProteinsData(Collection<String> uniProtIds) throws IOException {
		final ArrayList<String> uniProtIdsArray = new ArrayList<String>(uniProtIds);
		final List<Future<UniProtEntry>> requests = new ArrayList<Future<UniProtEntry>>();
		final ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		final CompletionService<UniProtEntry> completionService = new ExecutorCompletionService<UniProtEntry>(executor);
		
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
							if(!uniProtId.equals(null))
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
				if(result != null)
					results.put(uniProtIdsArray.get(requests.indexOf(take)), result);
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if(cause instanceof IOException)
					throw (IOException)cause;
				else
					cause.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}
}

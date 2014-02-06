package tk.nomis_tech.ppimapbuilder.orthology;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tk.nomis_tech.ppimapbuilder.networkbuilder.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.networkbuilder.data.UniProtProtein;

/**
 * Simple Java client for InParanoid database
 */
public class InParanoidClient {

	private static InParanoidClient _instance;
	
	private final String baseUrl = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi";
	private int NB_THREAD = 3;
	private double scoreLimit = 0.85;
	
	private InParanoidClient() {}

	/**
	 * Gets the singleton instance of InParanoidClient
	 */
	public static InParanoidClient getInstance() {
		if(_instance == null)
			_instance = new InParanoidClient();
		return _instance;
	}
	
	/**
	 * Search orthologs of a protein in a specified organisms
	 * @throws IOException if a connection error occurred 
	 */
	public HashMap<Integer, String> getOrthologs(String uniProtId, List<Integer> taxIds) throws IOException {
		HashMap<Integer, String> out = new HashMap<Integer, String>();

		// Create request URL
		StringBuilder reqURL = new StringBuilder(baseUrl)
				.append("?id=").append(uniProtId)
				.append("&idtype=proteinid")
				.append("&all_or_selection=all")
				//.append(";scorelimit=").append(scoreLimit) // <= Inparanoid doesn't filter score very well
				.append("&rettype=xml");

		// Add organism list to request URL
		for (Integer taxId : taxIds) {
			Integer org = Ortholog.translateTaxID(taxId);
			if(org != null) {
				reqURL.append("&specieslist=").append(org);
			}
			else System.err.println("Org id conversion failed [taxid:"+taxId+"]");
		}
		
		String req = reqURL.toString();
		
		Document doc = null;
		
		//Load xml response file (with multiple try)
		final int MAX_TRY = 2;
		int pos = 0;
		IOException lastError = null;
		do{
			try {
				doc = Jsoup.connect(req).get();
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500) return out; //protein ortholog not found
				lastError = new IOException(e);
			} catch(SocketTimeoutException e) {
				//Connection response timeout
				lastError = new IOException(e);
			}
		} while(++pos < MAX_TRY);
		
		if(doc == null) throw lastError;
		
		//For each cluster of ortholog
		for (Element speciesPair : doc.select("speciespair")) {
			try {
				int currentInpOrgID = Integer.valueOf(speciesPair.select("species").get(1).attr("id"));
				Integer currentTaxId = Ortholog.translateInparanoidID(currentInpOrgID);

				// If ortholog cluster correspond an organism asked in input
				if (currentTaxId != null && taxIds.contains(currentTaxId)) {
					String orthologFound = null;

					// Find the the protein ortholog (with the best score)
					Double betterScore = Double.NaN;
					for (Element protein : speciesPair.select("protein")) {
						try {
							if (Integer.valueOf(protein.attr("spec_id")) == currentInpOrgID) {
								double score = Double.valueOf(protein.attr("score"));

								if (betterScore.isNaN() || score > betterScore) {
									orthologFound = protein.attr("prot_id");
									betterScore = score;
								}
							}
						} finally {
						}
					}

					if(betterScore > scoreLimit)
						out.put(currentTaxId, orthologFound);
				}
			}
			finally {}
		}
		return out;
	}
	
	/**
	 * Search orthologs of proteins in desired organisms
	 */
	public HashMap<String, HashMap<Integer, String>> getOrthologsProteins(List<String> uniProtIds, final List<Integer> taxIds) throws IOException {
		final List<Future<HashMap<Integer, String>>> requests = new ArrayList<Future<HashMap<Integer, String>>>();
		final ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		final CompletionService<HashMap<Integer, String>> completionService = new ExecutorCompletionService<HashMap<Integer, String>>(executor);

		// For each protein => search orthologs in organisms
		for (int i = 0; i < uniProtIds.size(); i++) {
			final String uniProtId = uniProtIds.get(i);

			requests.add(completionService.submit(new Callable<HashMap<Integer, String>>() {
				@Override
				public HashMap<Integer, String> call() throws Exception {
					HashMap<Integer, String> result = null;
					
					final int MAX_TRY = 2;
					int i = 0;
					do {
						try {
							result = getOrthologs(uniProtId, taxIds);
						} catch (UnknownHostException e) {
							return null;
						} finally {
							i++;
						}
					} while (result == null || i > MAX_TRY);

					return result;
				}
			}));
		}

		// Collect all ortholog results
		final HashMap<String, HashMap<Integer, String>> results = new HashMap<String, HashMap<Integer, String>>();
		for (int i = 0; i < requests.size(); i++) {
			try {
				Future<HashMap<Integer, String>> take = completionService.take();
				HashMap<Integer, String> result = take.get();
				if(result != null)
					results.put(uniProtIds.get(requests.indexOf(take)), result);
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
	
	/**
	 * Search ortholog protein in a specified organism
	 * @throws IOException if a connection error occurred 
	 */
	public String getOrtholog(String uniProtId, Integer orthologNcbiTaxId) throws IOException {
		List<Integer> org = new ArrayList<Integer>();
		org.add(orthologNcbiTaxId);
		return getOrthologs(uniProtId, org).get(orthologNcbiTaxId);
	}

	/**
	 * Search ortholog for an UniProtProtein
	 * @param prot
	 *            the protein from which the ortholog will be retrieved
	 * @param taxid
	 *            the taxonomy identifier of the organism in which the ortholog will be searched
	 * @throws IOException
	 *             if connection error occurs
	 */
	public HashMap<String, HashMap<Integer, String>> searchOrthologForUniprotProtein(final List<UniProtProtein> prots, List<Integer> taxIds) throws IOException {
		List<String> protIds = new ArrayList<String>(){{
			for (UniProtProtein uniProtProtein : prots) {
				add(uniProtProtein.getUniprotId());
			}
		}};
		
		HashMap<String, HashMap<Integer, String>> orthologsProteins = getOrthologsProteins(protIds, taxIds);

		for (Map.Entry<String, HashMap<Integer, String>> orthologProts: orthologsProteins.entrySet()) {
			for(Map.Entry<Integer, String> ortholog: orthologProts.getValue().entrySet()) {
				for(UniProtProtein prot: prots) {
					if(prot.getUniprotId().equals(orthologProts.getKey())) {
						prot.addOrtholog(new OrthologProtein(ortholog.getValue(), ortholog.getKey()));
					}
				}
			}
		}
		
		return orthologsProteins;
	}
}

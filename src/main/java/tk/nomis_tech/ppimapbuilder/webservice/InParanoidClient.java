package tk.nomis_tech.ppimapbuilder.webservice;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.cytoscape.work.AbstractTask;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tk.nomis_tech.ppimapbuilder.data.Ortholog;
import tk.nomis_tech.ppimapbuilder.data.OrthologProtein;
import tk.nomis_tech.ppimapbuilder.data.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.networkbuilder.query.PMBQueryInteractionTask;

/**
 * Simple Java client for InParanoid database
 */
public class InParanoidClient {

	private static InParanoidClient _instance;
	
	private final String baseUrl = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi";
	
	final private int NB_THREAD;
	final private double scoreLimit;

	/**
	 * Constructs a InParanoid client with specified specified score limit and a default number of thread: 3
	 */
	public InParanoidClient(double scoreLimit) {
		this(3, scoreLimit);
	}
	
	/**
	 * Constructs a InParanoid client with specified number of thread and specified score
	 * @param pmbQueryInteractionTask 
	 */
	public InParanoidClient(int nB_THREAD, double scoreLimit) {
		NB_THREAD = nB_THREAD;
		this.scoreLimit = scoreLimit;
	}

	/**
	 * Search orthologs of a protein in a specified organisms
	 * @throws IOException if a connection error occurred 
	 */
	public HashMap<Integer, String> getOrthologsSingleProtein(String uniProtId, Collection<Integer> taxIds) throws IOException {
		HashMap<Integer, String> out = new HashMap<Integer, String>();
		
		// Create request parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", uniProtId);
		params.put("idtype", "proteinid");
		params.put("all_or_selection", "all");
		params.put("rettype", "xml");

		/*// Add organism list to request URL
		for (Integer taxId : taxIds) {
			Integer org = Ortholog.translateTaxID(taxId);
			if(org != null) {
				reqURL.append("&specieslist=").append(org);
			}
			else System.err.println("Org id conversion failed [taxid:"+taxId+"]");
		}*/
		
		//System.out.println(req);
		
		Document doc = null;
		
		// Load xml response file (with multiple try)
		final int MAX_TRY = 5;
		int pos = 1;
		IOException lastError = null;
		do{
			try {
				Connection connect = Jsoup.connect(baseUrl);
				connect.timeout(18000+(18000*pos)/3);
				connect.data(params);
				pos++;
				doc = connect.get();
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500) 
					return out; //protein ortholog not found or inparanoid server down
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw new IOException(e);
				}
				
				//lastError = new IOException(e);
				throw new IOException(e);
			} catch(SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			}
		} while(doc == null);// && ++pos <= MAX_TRY);

		System.out.print("p"+pos+"-");
		
		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");
		
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
	public HashMap<String, HashMap<Integer, String>> getOrthologsMultipleProtein(Collection<String> uniProtIds, final Collection<Integer> taxIds) throws IOException, UnknownHostException {
		final List<Future<HashMap<Integer, String>>> requests = new ArrayList<Future<HashMap<Integer, String>>>();
		final ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		final CompletionService<HashMap<Integer, String>> completionService = new ExecutorCompletionService<HashMap<Integer, String>>(executor);

		
		// For each protein => search orthologs in organisms
		for (final String uniProtId: uniProtIds) {
			requests.add(completionService.submit(new Callable<HashMap<Integer, String>>() {
				@Override
				public HashMap<Integer, String> call() throws Exception {
					return getOrthologsSingleProtein(uniProtId, taxIds);
				}
			}));
		}
		
		

		// Collect all ortholog results
		final List<String> uniProtIdsArray = new ArrayList<String>(uniProtIds);
		final HashMap<String, HashMap<Integer, String>> results = new HashMap<String, HashMap<Integer, String>>();
		
		for (Future<HashMap<Integer, String>> req: requests) {
			try {
				Future<HashMap<Integer, String>> take = completionService.take();
				HashMap<Integer, String> result = take.get();
				if(result != null)
					results.put(uniProtIdsArray.get(requests.indexOf(take)), result);

			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if(cause instanceof UnknownHostException)
					throw (UnknownHostException) cause;
				if(cause instanceof SocketTimeoutException)
					continue;
				if(cause instanceof IOException)
					throw (IOException) cause;
				else
					cause.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		

		System.out.println("\n--");
		return results;
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
	public HashMap<String, HashMap<Integer, String>> searchOrthologForUniprotProtein(final Collection<UniProtEntry> prots, Collection<Integer> taxIds) throws IOException {
		final List<Integer> taxIdsArray = new ArrayList<Integer>(taxIds);
		
		
		List<String> protIds = new ArrayList<String>(){{
			Iterator<UniProtEntry> it = prots.iterator();
			while (it.hasNext()) {
				add(((UniProtEntry) it.next()).getUniprotId());
			}
		}};
		
		
		HashMap<String, HashMap<Integer, String>> orthologsProteins = getOrthologsMultipleProtein(protIds, taxIdsArray);

		for (Map.Entry<String, HashMap<Integer, String>> orthologProts: orthologsProteins.entrySet()) {
			for(Map.Entry<Integer, String> ortholog: orthologProts.getValue().entrySet()) {
				Iterator<UniProtEntry> it = prots.iterator();
				while (it.hasNext()) {
					UniProtEntry prot = (UniProtEntry) it.next();
					if(prot.getUniprotId().equals(orthologProts.getKey())) {
						prot.addOrtholog(new OrthologProtein(ortholog.getValue(), ortholog.getKey()));
					}
				}
			}
		}
		
		
		return orthologsProteins;
	}
	
	
	
	/**
	 * Search protein ortholog for the reference organism
	 * @throws IOException if a connection error occurred 
	 */
	public String getOrthologForRefOrga(String uniProtId, Integer taxId) throws IOException {
		String out = new String();
		
		// Create request parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", uniProtId);
		params.put("idtype", "proteinid");
		params.put("all_or_selection", "all");
		params.put("rettype", "xml");
		
		Document doc = null;
		
		// Load xml response file (with multiple try)
		final int MAX_TRY = 5;
		int pos = 1;
		IOException lastError = null;
		do{
			try {
				Connection connect = Jsoup.connect(baseUrl);
				connect.timeout(18000+(18000*pos)/3);
				connect.data(params);
				pos++;
				doc = connect.get();
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500) 
					return out; //protein ortholog not found or inparanoid server down
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw new IOException(e);
				}
				
				//lastError = new IOException(e);
				throw new IOException(e);
			} catch(SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			}
		} while(doc == null);// && ++pos <= MAX_TRY);

		System.out.print("p"+pos+"-");
		
		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");
		
		//For each cluster of ortholog
		for (Element speciesPair : doc.select("speciespair")) {
			try {
				int currentInpOrgID = Integer.valueOf(speciesPair.select("species").get(1).attr("id"));
				Integer currentTaxId = Ortholog.translateInparanoidID(currentInpOrgID);

				// If ortholog cluster correspond an organism asked in input
				if (currentTaxId != null && taxId.equals(currentTaxId)) {
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
						out = orthologFound;
				}
			}
			finally {}
		}
		return out;
	}
	
}

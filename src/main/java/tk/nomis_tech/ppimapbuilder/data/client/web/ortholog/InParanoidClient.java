package tk.nomis_tech.ppimapbuilder.data.client.web.ortholog;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tk.nomis_tech.ppimapbuilder.data.client.ProteinOrthologClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple Java client for InParanoid database
 */
public class InParanoidClient extends ProteinOrthologClient {

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
	 */
	public InParanoidClient(int NB_THREAD, double scoreLimit) {
		this.NB_THREAD = NB_THREAD;
		this.scoreLimit = scoreLimit;
	}

	/**
	 * Search orthologs of a protein in a specified organisms
	 *
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
		final int MAX_TRY = 2;
		int pos = 1;
		IOException lastError = null;
		do {
			try {
				Connection connect = Jsoup.connect(baseUrl);
				connect.timeout(18000 + (18000 * pos) / 3);
				connect.data(params);
				pos++;
				doc = connect.get();
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500)
					return out; //protein organism not found or inparanoid server down
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw new IOException(e);
				}

				//lastError = new IOException(e);
				//throw new IOException(e);
			} catch (SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			}
		} while (doc == null && ++pos <= MAX_TRY);

		System.out.print("p" + pos + "-");

		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");

		//For each cluster of organism
		for (Element speciesPair : doc.select("speciespair")) {
			try {
				int currentInpOrgID = Integer.valueOf(speciesPair.select("species").get(1).attr("id"));
				Integer currentTaxId = OrganismRepository.getInstance().getOrganismByInParanoidOrgId(currentInpOrgID).getTaxId();

				// If organism cluster correspond an organism asked in input
				if (currentTaxId != null && taxIds.contains(currentTaxId)) {
					String orthologFound = null;

					// Find the the protein organism (with the best score)
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

					if (betterScore > scoreLimit)
						out.put(currentTaxId, orthologFound);
				}
			} finally {
			}
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
		for (final String uniProtId : uniProtIds) {
			requests.add(completionService.submit(new Callable<HashMap<Integer, String>>() {
				@Override
				public HashMap<Integer, String> call() throws Exception {
					return getOrthologsSingleProtein(uniProtId, taxIds);
				}
			}));
		}


		// Collect all organism results
		final List<String> uniProtIdsArray = new ArrayList<String>(uniProtIds);
		final HashMap<String, HashMap<Integer, String>> results = new HashMap<String, HashMap<Integer, String>>();

		for (Future<HashMap<Integer, String>> req : requests) {
			try {
				Future<HashMap<Integer, String>> take = completionService.take();
				HashMap<Integer, String> result = take.get();
				if (result != null)
					results.put(uniProtIdsArray.get(requests.indexOf(take)), result);

			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof UnknownHostException)
					throw (UnknownHostException) cause;
				if (cause instanceof SocketTimeoutException)
					continue;
				if (cause instanceof IOException)
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
	 * Search protein organism for the reference organism
	 *
	 * @throws IOException if a connection error occurred
	 */
	public String getOrthologForRefOrga(String uniProtId, Integer taxId) throws IOException {
		String out = "";

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
		do {
			try {
				Connection connect = Jsoup.connect(baseUrl);
				connect.timeout(18000 + (18000 * pos) / 3);
				connect.data(params);
				pos++;
				doc = connect.get();
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500)
					return out; //protein organism not found or inparanoid server down
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw new IOException(e);
				}

				//lastError = new IOException(e);
				throw new IOException(e);
			} catch (SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			}
		} while (doc == null);// && ++pos <= MAX_TRY);

		System.out.print("p" + pos + "-");

		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");

		//For each cluster of organism
		for (Element speciesPair : doc.select("speciespair")) {
			try {
				int currentInpOrgID = Integer.valueOf(speciesPair.select("species").get(1).attr("id"));
				Integer currentTaxId = OrganismRepository.getInstance().getOrganismByInParanoidOrgId(currentInpOrgID).getTaxId();

				// If organism cluster correspond an organism asked in input
				if (currentTaxId != null && taxId.equals(currentTaxId)) {
					String orthologFound = null;

					// Find the the protein organism (with the best score)
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

					if (betterScore > scoreLimit)
						out = orthologFound;
				}
			} finally {
			}
		}
		return out;
	}


	@Override
	public Map<Organism, Protein> getOrthologsMultiOrganism(Protein protein, List<Organism> organisms) throws IOException {

		Map<Organism, Protein> out = new HashMap<Organism, Protein>();

		// Create request parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", protein.getUniProtId());
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
		final int MAX_TRY = 2;
		int pos = 1;
		IOException lastError = null;
		do {
			try {
				Connection connect = Jsoup.connect(baseUrl);
				connect.timeout(18000 + (18000 * pos) / 3);
				connect.data(params);
				pos++;
				doc = connect.get();
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500)
					return out; //no entry found or inparanoid server down
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw new IOException(e);
				}

				//lastError = new IOException(e);
				//throw new IOException(e);
			} catch (SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			}
		} while (doc == null && ++pos <= MAX_TRY);

		System.out.print("p" + pos + "-");

		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");

		// For each species pair
		for (Element speciesPair : doc.select("speciespair")) {
			Elements species = speciesPair.select("species");

			Organism orgA = OrganismRepository.getInstance().getOrganismByCommonName(species.get(0).attr("speclong"));
			Organism orgB = OrganismRepository.getInstance().getOrganismByCommonName(species.get(1).attr("speclong"));

			Organism currentOrg;

			if (organisms.contains(orgA) && organisms.contains(orgB))
				break;
			else if (orgA.equals(protein.getOrganism()) && organisms.contains(orgB)) {
				currentOrg = orgB;
			} else if (orgB.equals(protein.getOrganism()) && organisms.contains(orgA)) {
				currentOrg = orgA;
			} else break;


			Protein ortholog = null;
			Double betterScore = Double.NaN;

			// For each protein of this species pair get the protein from current organism with best score
			for (Element proteinElement : speciesPair.select("protein")) {
				Organism proteinOrg = OrganismRepository.getInstance().getOrganismByCommonName(proteinElement.attr("speclong"));

				if (proteinOrg.equals(currentOrg)) {
					double score = Double.valueOf(proteinElement.attr("score"));

					if (betterScore.isNaN() || score > betterScore) {
						betterScore = score;
						ortholog = new Protein(proteinElement.attr("prot_id"), currentOrg);

						if (protein instanceof UniProtEntry) {
							((UniProtEntry) protein).addOrtholog(ortholog);
						}
					}
				}
			}

			// If ortholog found with best score has sufficient score
			if (ortholog != null && betterScore > scoreLimit) {
				// Add ortholog to output
				out.put(currentOrg, ortholog);
			}
		}

		return out;
	}

	@Override
	public Protein getOrtholog(Protein protein, Organism organism) throws IOException {
		return getOrthologsMultiOrganism(protein, Arrays.asList(organism)).get(organism);
	}
}

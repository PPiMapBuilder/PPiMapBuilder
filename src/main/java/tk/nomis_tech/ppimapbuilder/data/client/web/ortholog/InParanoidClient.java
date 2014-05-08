package tk.nomis_tech.ppimapbuilder.data.client.web.ortholog;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tk.nomis_tech.ppimapbuilder.data.client.AbstractProteinOrthologClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Java client for InParanoid database
 */
public class InParanoidClient extends AbstractProteinOrthologClient {

	private final String BASE_URL = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi";

	private final double SCORE_LIMIT;

	/**
	 * Simple inParanoid cache of XML responses indexed by UniProt identifier
	 */
	private Map<String, Document> inParanoidEntryCache;

	/**
	 * Constructs a InParanoid client with specified number of thread and specified score
	 */
	public InParanoidClient(int nThread, double SCORE_LIMIT) {
		super(nThread);
		this.SCORE_LIMIT = SCORE_LIMIT;
	}

	public InParanoidClient(double scoreLimit) {
		this(9, scoreLimit);
	}

	/**
	 * Enables or disables the inParanoid cache.
	 * This cache keeps in memory the inParanoid request response for each queried proteins.
	 * If enabled, make sure to disable it after a long session of ortholog search to free the memory of the cache.
	 *
	 * @param enable true to activate the cache; false to
	 */
	public void enableCache(boolean enable) {
		if (enable && inParanoidEntryCache == null)
			inParanoidEntryCache = new HashMap<String, Document>();
		else if (!enable)
			inParanoidEntryCache = null;
	}

	private Document searchOrthologEntry(Protein protein, int timeout) throws IOException {
		// Create request parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", protein.getUniProtId());
		params.put("idtype", "proteinid");
		params.put("all_or_selection", "all");
		params.put("rettype", "xml");

		// Create JSoup connection
		Connection connect = Jsoup.connect(BASE_URL);
		connect.timeout(timeout);
		connect.data(params);
		return connect.get();
	}

	private Document searchOrthologEntryWithRetryAndCache(final Protein protein) throws IOException {
		Document doc = null;

		// Load xml response file (with multiple try)
		final int MAX_TRY = 3;
		int pos = 1;
		IOException lastError = null;
		do {
			try {
				// sleep to temporize requests
				if (pos > 1)
					Thread.sleep(500);

				final int timeout = 18000 + (18000 * pos) / 3;

				System.out.print("inparanoid-");
				// Cache enabled?
				if (inParanoidEntryCache != null) {
					// Thread-safe to ensure that only one inParanoid request is made for a protein.
					// (and then cached so that other thread can get the XML document without re-requesting it)
					synchronized (protein.getUniProtId()) {
						doc = inParanoidEntryCache.get(protein.getUniProtId());

						if (doc == null) {
							doc = searchOrthologEntry(protein, timeout);

							inParanoidEntryCache.put(protein.getUniProtId(), doc);
						} else System.out.print("cached-");
						break;
					}
				}

				doc = searchOrthologEntry(protein, timeout);
				break;
			} catch (HttpStatusException e) {
				if (e.getStatusCode() == 500)
					break; //no entry found or inParanoid server error
				if (e.getStatusCode() == 503 || e.getStatusCode() == 504) {
					throw e;
				}

				//lastError = new IOException(e);
				//throw new IOException(e);
			} catch (SocketTimeoutException e) {
				//Connection response timeout
				//lastError = new IOException(e);
				throw new IOException(e);
			} catch (InterruptedException e) {
			}
		} while (doc == null && ++pos <= MAX_TRY);

		System.out.print(protein.getUniProtId() + ":" + pos + "try");

		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");

		return doc;
	}

	@Override
	public Protein getOrtholog(Protein protein, Organism organism) throws IOException {
		Document doc = searchOrthologEntryWithRetryAndCache(protein);

		if (doc == null) return null;
		System.out.println(":OK:" +organism);

		// For each species pair
		for (Element speciesPair : doc.select("speciespair")) {
			Elements species = speciesPair.select("species");

			String nameA = species.get(0).attr("speclong");
			String nameB = species.get(1).attr("speclong");
			Organism orgA = UserOrganismRepository.getInstance().getOrganismBySimpleName(nameA);
			Organism orgB = UserOrganismRepository.getInstance().getOrganismBySimpleName(nameB);

			if (orgA == null || orgB == null) continue;

			Organism currentOrg = null;
			if (orgA.equals(protein.getOrganism()) && organism.equals(orgB)) {
				currentOrg = orgB;
			} else if (orgB.equals(protein.getOrganism()) && organism.equals(orgA)) {
				currentOrg = orgA;
			}

			if (currentOrg == null) continue;

			Protein ortholog = null;
			Double betterScore = Double.NaN;

			// For each protein of this species pair get the protein from current organism with best score
			for (Element proteinElement : speciesPair.select("protein")) {
				Organism proteinOrg = UserOrganismRepository.getInstance().getOrganismBySimpleName(proteinElement.attr("speclong"));

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
			if (ortholog != null && betterScore > SCORE_LIMIT) {
				// return ortholog
				return ortholog;
			}
		}

		return null;
	}

	@Override
	public Map<Protein, Map<Organism, Protein>> getOrthologsMultiOrganismMultiProtein(List<Protein> proteins, List<Organism> organisms) throws IOException {
		boolean cacheEnabled = (inParanoidEntryCache != null);

		if (!cacheEnabled) enableCache(true);
		Map<Protein, Map<Organism, Protein>> result = super.getOrthologsMultiOrganismMultiProtein(proteins, organisms);
		if (!cacheEnabled) enableCache(false);

		return result;
	}

	@Override
	public Map<Organism, Protein> getOrthologsMultiOrganism(Protein protein, List<Organism> organisms) throws IOException {
		boolean cacheEnabled = (inParanoidEntryCache != null);

		if (!cacheEnabled) enableCache(true);
		Map<Organism, Protein> result = super.getOrthologsMultiOrganism(protein, organisms);
		if (!cacheEnabled) enableCache(false);

		return result;
	}

}

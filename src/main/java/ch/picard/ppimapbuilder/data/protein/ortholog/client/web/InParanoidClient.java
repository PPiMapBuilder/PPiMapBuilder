package ch.picard.ppimapbuilder.data.protein.ortholog.client.web;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Java client for InParanoid database
 */
public class InParanoidClient extends AbstractProteinOrthologClient {

	private final String BASE_URL = "http://inparanoid.sbc.su.se/cgi-bin/gene_search.cgi";

	/**
	 * Simple inParanoid cache of XML responses indexed by UniProt identifier
	 */
	private final List<String> lastUniprotId = new ArrayList<String>();
	private Map<String, Document> inParanoidEntryCache;

	/**
	 * Enables or disables the inParanoid cache.
	 * This cache keeps in memory the inParanoid request response for each queried proteins.
	 * If enabled, make sure to disable it after a long session of ortholog search to free the memory of the cache.
	 *
	 * @param enable true to activate the cache; false to
	 */
	public void enableCache(boolean enable) {
		if (enable && inParanoidEntryCache == null) {
			inParanoidEntryCache = new HashMap<String, Document>();
			lastUniprotId.clear();
		} else if (!enable) {
			lastUniprotId.clear();
			inParanoidEntryCache = null;
		}
	}

	public boolean cacheEnabled() {
		return inParanoidEntryCache == null;
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
							if(inParanoidEntryCache.size() > 30) {
								String lastUsedUniprot = lastUniprotId.get(lastUniprotId.size() - 1);
								inParanoidEntryCache.remove(lastUsedUniprot);
								lastUniprotId.remove(lastUsedUniprot);
							}
							lastUniprotId.add(protein.getUniProtId());
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
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		Document doc = searchOrthologEntryWithRetryAndCache(protein);

		if (doc == null) return null;
		//System.out.println(":OK:" + organism);

		OrthologGroup group = new OrthologGroup();

		// For each species pair
		for (Element speciesPair : doc.select("speciespair")) {
			Elements species = speciesPair.select("species");

			String nameA = species.get(0).attr("speclong");
			String nameB = species.get(1).attr("speclong");
			Organism orgA = UserOrganismRepository.getInstance().getOrganismBySimpleName(nameA);
			Organism orgB = UserOrganismRepository.getInstance().getOrganismBySimpleName(nameB);

			if (orgA == null || orgB == null) continue;

			Organism otherOrganism = null;
			if (orgA.equals(protein.getOrganism()) && organism.equals(orgB)) {
				otherOrganism = orgB;
			} else if (orgB.equals(protein.getOrganism()) && organism.equals(orgA)) {
				otherOrganism = orgA;
			}

			if (otherOrganism == null) continue;

			// For each protein of this species pair get the protein from current organism with best score
			Protein ortholog;
			Double score;
			for (Element proteinElement : speciesPair.select("protein")) {
				Organism proteinOrg = UserOrganismRepository.getInstance().getOrganismBySimpleName(proteinElement.attr("speclong"));

				score = Double.valueOf(proteinElement.attr("score"));
				ortholog = new Protein(proteinElement.attr("prot_id"), proteinOrg);

				group.add(new OrthologScoredProtein(ortholog, score));
			}
		}

		if(group.isValid())
			return group;
		else
			return null;
	}
}

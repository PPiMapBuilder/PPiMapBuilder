package ch.picard.ppimapbuilder.data.protein.ortholog.client.web;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.ProteinOrthologCacheClient;
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
	private ProteinOrthologCacheClient cache = null;

	/**
	 * The InParanoid client has no cache built-in. With this method you can specify a cache in which the client will
	 * store all OthologGroup encountered while searching for the asked one.
	 */
	public void setCache(ProteinOrthologCacheClient cache) {
		this.cache = cache;
	}

	public boolean cacheEnabled() {
		return cache == null;
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
		//IOException lastError = null;
		do {
			try {
				// sleep to temporize requests
				if (pos > 1)
					Thread.sleep(500);

				final int timeout = 18000 + (18000 * pos) / 3;

				System.out.print("inparanoid-");
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
			} catch (InterruptedException ignored) {
			}
		} while (++pos <= MAX_TRY && doc == null);

		System.out.print(protein.getUniProtId() + ":" + pos + "try");

		//if(doc == null) throw lastError;
		//System.out.println("done with "+(pos-1)+" try");

		return doc;
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		Document doc = searchOrthologEntryWithRetryAndCache(protein);
		if (doc == null) return null;

		// For each species pair
		OrthologGroup correctGroup = null;
		for (Element speciesPair : doc.select("speciespair")) {
			OrthologGroup group = new OrthologGroup();

			// For each protein of this species pair get the protein from current organism with best score
			Protein ortholog;
			Double score;
			Organism otherOrganism = null;
			for (Element proteinElement : speciesPair.select("protein")) {
				Organism proteinOrg = InParanoidOrganismRepository.getInstance().getOrganismBySimpleName(proteinElement.attr("speclong"));
				if (proteinOrg == null)
					continue;
				else if (otherOrganism == null) {
					otherOrganism = !proteinOrg.equals(protein.getOrganism()) ? proteinOrg : null;
				}

				score = Double.valueOf(proteinElement.attr("score"));
				ortholog = new Protein(proteinElement.attr("prot_id"), proteinOrg);

				group.add(new OrthologScoredProtein(ortholog, score));
			}

			if (group.isValid()) {
				if(organism.equals(otherOrganism))
					correctGroup = group;

				if(cache != null)
					cache.addOrthologGroup(group);
			}
		}

		return correctGroup;
	}
}

package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;

/**
 * A protein ortholog client with multiple source : a cache and a web client.
 * When an ortholog is requested, the cache is searched first.
 * If not found in cache, the ortholog is searched with a web client and the result is stored in the cache.
 */
public class ProteinOrthologWebCachedClient extends AbstractProteinOrthologClient {

	private final PMBProteinOrthologCacheClient cacheClient;
	private final InParanoidClient webClient;

	public ProteinOrthologWebCachedClient(InParanoidClient webClient, PMBProteinOrthologCacheClient cacheClient) {
		this.cacheClient = cacheClient;
		this.webClient = webClient;
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		OrthologGroup group = null;
		boolean cacheIsFull = false;

		if(cacheClient != null) {
			group = cacheClient.getOrthologGroup(protein, organism);
			cacheIsFull = cacheClient.isFull(protein.getOrganism(), organism);
		}

		if (group == null && !cacheIsFull) {
			if(webClient != null)
				group = webClient.getOrthologGroup(protein, organism);
			if(cacheClient != null && group != null)
				cacheClient.addOrthologGroup(group);
		} //else System.out.println("cache:"+protein.getUniProtId()+":"+organism);

		return group;
	}

}

package tk.nomis_tech.ppimapbuilder.data.client;

import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.ProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.io.IOException;

/**
 * A protein ortholog client with multiple source of protein ortholog: a cache and a web client.
 * When an ortholog is requested, the cache is search first.
 * If not found in cache, the ortholog is searched with a web client and the result is stored in the cache.
 */
public class ProteinOrthologWebCachedClient extends AbstractProteinOrthologClient {

	private ProteinOrthologCacheClient cacheClient;
	private AbstractProteinOrthologClient webClient;

	@Override
	public Protein getOrtholog(Protein protein, Organism organism) throws IOException {
		Protein ortholog = null;

		if(cacheClient != null)
			ortholog = cacheClient.getOrtholog(protein, organism);

		if (ortholog == null) {
			if(webClient != null)
				ortholog = webClient.getOrtholog(protein, organism);
			if(cacheClient != null && ortholog != null)
				cacheClient.addOrthologGroup(protein, ortholog);
		} else System.out.println("cache:"+protein.getUniProtId()+":"+organism);

		return ortholog;
	}

	public void setCacheClient(ProteinOrthologCacheClient cacheClient) {
		this.cacheClient = cacheClient;
	}

	public void setWebClient(AbstractProteinOrthologClient webClient) {
		this.webClient = webClient;
	}
}

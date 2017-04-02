/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
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

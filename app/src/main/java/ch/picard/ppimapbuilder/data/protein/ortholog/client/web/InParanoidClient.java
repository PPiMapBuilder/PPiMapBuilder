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
    
package ch.picard.ppimapbuilder.data.protein.ortholog.client.web;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.ProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.util.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		URIBuilder builder = new URIBuilder(BASE_URL)
				.addParameter("id", protein.getUniProtId())
				.addParameter("idtype", "proteinid")
				.addParameter("all_or_selection", "all")
				.addParameter("rettype", "xml");

		Document doc = IOUtils.getDocumentWithRetry(builder.build().toString(), 1500, 3000, 3, 500);
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

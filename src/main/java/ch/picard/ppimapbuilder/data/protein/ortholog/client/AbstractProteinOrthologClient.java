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
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProteinOrthologClient implements ProteinOrthologClient {

	@Override
	public List<OrthologScoredProtein> getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		OrthologGroup group = getOrthologGroup(protein, organism);

		if (group == null && !ProteinUtils.UniProtId.isStrict(protein.getUniProtId())) {
			String id = ProteinUtils.UniProtId.extractStrictUniProtId(protein.getUniProtId());

			group = getOrthologGroup(new Protein(id, protein.getOrganism()), organism);
		}

		if (group != null) {
			List<OrthologScoredProtein> ortholog = group.getBestOrthologsInOrganism(organism);
			OrthologScoredProtein originalProtein = group.find(protein);

			if (!ortholog.isEmpty() && ortholog.get(0).getScore() >= score
					&& originalProtein != null && originalProtein.getScore() >= score)
				return ortholog;
		}
		return new ArrayList<OrthologScoredProtein>();
	}

}


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
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.List;

public interface ProteinOrthologClient {

	/**
	 * Gets the orthologous proteins of the given protein in the specified destination organism with a specified
	 * minimum score. Only the top scored orthologs will be returned
	 */
	public List<OrthologScoredProtein> getOrtholog(Protein protein, Organism organism, Double score) throws Exception;

	/**
	 * Gets the {@link OrthologGroup} in which the given protein appear with ortholog of the specified organism
	 */
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception;
}

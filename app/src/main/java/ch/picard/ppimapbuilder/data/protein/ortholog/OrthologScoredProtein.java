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
    
package ch.picard.ppimapbuilder.data.protein.ortholog;

import ch.picard.ppimapbuilder.data.protein.Protein;

/**
 * Protein model associated with a score.
 */
public class OrthologScoredProtein extends Protein {

	private static final long serialVersionUID = 1L;

	private final Double score;

	public OrthologScoredProtein(Protein protein, Double score) {
		super(protein.getUniProtId(), protein.getOrganism());
		this.score = score;
	}

	public Double getScore() {
		return score;
	}

}
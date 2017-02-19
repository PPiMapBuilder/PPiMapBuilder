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
    
package ch.picard.ppimapbuilder.data.ontology;

import java.io.Serializable;

public enum GeneOntologyCategory implements Serializable {

	MOLECULAR_FUNCTION,
	BIOLOGICAL_PROCESS,
	CELLULAR_COMPONENT;

	public static GeneOntologyCategory getByLetter(char letter) {
		switch (letter) {
			case 'C' : return GeneOntologyCategory.CELLULAR_COMPONENT;
			case 'F' : return GeneOntologyCategory.MOLECULAR_FUNCTION;
			case 'P' : return GeneOntologyCategory.BIOLOGICAL_PROCESS;
			default : return null;
		}
	}

	@Override
	public String toString() {
		switch (this) {
			case MOLECULAR_FUNCTION: return "Molecular function";
			case BIOLOGICAL_PROCESS: return "Biological process";
			case CELLULAR_COMPONENT: return "Cellular component";
		}
		return null;
	}
}

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
    
package ch.picard.ppimapbuilder.data.organism;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.PairUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganismUtils {

	private static final Pattern pattern = Pattern.compile("\\(.*\\)");

	/**
	 * Creates pair combination of organisms using an organism list.
	 */
	public static Set<Pair<Organism>> createCombinations(List<Organism> organisms) {
		return PairUtils.createCombinations(new HashSet<Organism>(organisms), false, true);
	}

	public static List<String> organismsToStrings(Collection<Organism> organisms) {
		ArrayList<String> out = new ArrayList<String>();
		for(Organism organism : organisms) {
			out.add(organism.getScientificName());
		}
		return out;
	}

	@Deprecated
	public static Organism findOrganismInMITABTaxId(OrganismRepository repository, String taxId) {
		Matcher matcher;
		int taxIdA = Integer.parseInt(taxId);
		Organism organism = repository.getOrganismByTaxId(taxIdA);

		/*
		if(organism == null && (matcher = pattern.matcher(taxId)).matches()) {
			String[] genusSpecies = matcher.group(1).split(" ");
			organism = InParanoidOrganismRepository.getInstance().getOrganismByGenusAndSpecies(
					genusSpecies[0],
					genusSpecies[1]
			);
		}*/
		return organism;
	}

}

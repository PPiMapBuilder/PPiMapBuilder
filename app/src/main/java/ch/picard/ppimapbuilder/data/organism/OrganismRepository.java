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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OrganismRepository {

    protected final List<Organism> organisms;

    OrganismRepository(Organism... organisms) {
        this.organisms = Arrays.asList(organisms);
    }

    OrganismRepository(List<Organism> organisms) {
        this.organisms = new ArrayList<Organism>(organisms);
    }

	public Organism getOrganismByGenusAndSpecies(String genus, String species) {
		for (Organism organism : organisms) {
			if(organism.getGenus().equalsIgnoreCase(genus) && organism.getSpecies().equalsIgnoreCase(species))
				return organism;
		}
		return null;
	}

    public Organism getOrganismBySimpleName(String simpleName) {
        for (Organism org : organisms)
            if (simpleName.startsWith(org.getSimpleScientificName()))
                return org;
        return null;
    }

    public List<Organism> getOrganisms() {
        return organisms;
    }

    public List<String> getOrganismNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Organism o : getOrganisms()) {
            names.add(o.getScientificName());
        }
        return names;

    }

    public Organism getOrganismByScientificName(String scientificName) {
        for (Organism o : organisms) {
            if (o.getScientificName().equals(scientificName)) {
                return o;
            }
        }
        return null;
    }

    public Organism getOrganismByTaxId(int taxId) {
        for (Organism org : organisms)
            if (org.getTaxId() == taxId)
                return org;
        return null;
    }

}

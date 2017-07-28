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

import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.List;

/**
 * Class that keeps a register of organisms in PMB
 */
public class UserOrganismRepository extends OrganismRepository {

	private static UserOrganismRepository _instance;

	public static UserOrganismRepository getInstance() {
		if (_instance == null)
			_instance = new UserOrganismRepository();
		return _instance;
	}

	private UserOrganismRepository() {
		super(PMBSettings.getInstance().getOrganismList());
	}
	
	public static void resetToSettings() {
		_instance = new UserOrganismRepository();
	}
	
	public static List<Organism> getDefaultOrganismList() {
		List<Integer> taxIds = Lists.newArrayList(
				9606, 3702, 6239, 7227, 10090, 559292, 284812, 36329, 9031
		);
		List<Organism> organisms = Lists.newArrayList();

		for (Integer taxId : taxIds) {
		    organisms.add(
                    InParanoidOrganismRepository.getInstance().getOrganismByTaxId(taxId)
			);
		}

		return organisms;
	}

	public void removeOrganism(Organism o) {
		organisms.remove(o);
		PMBProteinOrthologCacheClient.getInstance().emptyCacheLinkedToOrganism(o);
	}
	
	public void addOrganism(String scientificName) {
		Organism orga = InParanoidOrganismRepository.getInstance().getOrganismByScientificName(scientificName);		
		if (orga != null) {
            organisms.add(orga);
		}
	}
	
	public void removeOrganismExceptLastOne(String scientificName) {
		if (organisms.size() > 1)
			removeOrganism(getOrganismByScientificName(scientificName));
		else 
			JOptionPane.showMessageDialog(null, "Please keep at least one organism", "Deletion impossible", JOptionPane.INFORMATION_MESSAGE);
	}
	
}

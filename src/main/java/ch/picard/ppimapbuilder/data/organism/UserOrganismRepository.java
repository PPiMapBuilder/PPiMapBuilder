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

import javax.swing.*;
import java.util.ArrayList;
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

	private UserOrganismRepository(List<Organism> organismList) {
		super(organismList);
	}
	private UserOrganismRepository() {
		this(PMBSettings.getInstance().getOrganismList());
	}
	
	public static void resetToSettings() {
		_instance = new UserOrganismRepository();
	}
	
	public static ArrayList<Organism> getDefaultOrganismList() {
		ArrayList<Organism> organismList = new ArrayList<Organism>();
		organismList.add(new Organism("Homo sapiens", "HUMAN", "Human", 9606));
		organismList.add(new Organism("Arabidopsis thaliana", "ARATH", "Mouse-ear cress", 3702));
		organismList.add(new Organism("Caenorhabditis elegans", "CAEEL", "", 6239));
		organismList.add(new Organism("Drosophila melanogaster", "DROME", "Fruit fly", 7227));
		organismList.add(new Organism("Mus musculus", "MOUSE", "Mouse", 10090));
		organismList.add(new Organism("Saccharomyces cerevisiae (strain ATCC 204508 / S288c)", "YEAST", "Baker's yeast", 559292));
		organismList.add(new Organism("Schizosaccharomyces pombe (strain 972 / ATCC 24843)", "SCHPO", "Fission yeast", 284812));
		organismList.add(new Organism("Plasmodium falciparum (isolate 3D7)", "PLAF7", "", 36329));
		organismList.add(new Organism("Gallus gallus", "CHICK", "Chicken", 9031));
		return organismList;
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

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
    
package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GOSlimRepository {

	private static GOSlimRepository _instance;

	public static GOSlimRepository getInstance() {
		if(_instance == null)
			_instance = new GOSlimRepository();
		return _instance;
	}

	private final Map<String, GOSlim> goSlims;

	private GOSlimRepository() {
		this.goSlims = new HashMap<String, GOSlim>();
		for(GOSlim geneOntologyTermSet : PMBSettings.getInstance().getGoSlimList())
			this.goSlims.put(geneOntologyTermSet.getName(), geneOntologyTermSet);
	}

	public boolean addGOSlim(GOSlim goSlim) {
		if(goSlims.containsKey(goSlim.getName()))
			return false;
		goSlims.put(goSlim.getName(), goSlim);
		return true;
	}

	public GeneOntologyTermSet getGOSlim(String name) {
		return goSlims.get(name);
	}

	public List<String> getGOSlimNames() {
		return new ArrayList<String>(goSlims.keySet());
	}

	public static void resetToSettings() {
		_instance = new GOSlimRepository();
	}

	public List<GOSlim> getGOSlims() {
		return new ArrayList<GOSlim>(goSlims.values());
	}

	public void remove(String name) {
		goSlims.remove(name);
	}
}

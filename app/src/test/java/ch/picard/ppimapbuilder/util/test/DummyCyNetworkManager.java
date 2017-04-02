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
    
package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import java.util.Set;

public class DummyCyNetworkManager implements CyNetworkManager {
	@Override
	public Set<CyNetwork> getNetworkSet() {
		return null;
	}

	@Override
	public CyNetwork getNetwork(long l) {
		return null;
	}

	@Override
	public boolean networkExists(long l) {
		return false;
	}

	@Override
	public void destroyNetwork(CyNetwork cyNetwork) {

	}

	@Override
	public void addNetwork(CyNetwork cyNetwork) {

	}

	@Override
	public void reset() {

	}
}

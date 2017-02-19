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
import org.cytoscape.model.CyNode;

public class DummyCyNode implements CyNode {
	private CyNetwork cyNetwork;
	private String name;

	DummyCyNode(CyNetwork cyNetwork) {
		this.cyNetwork = cyNetwork;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public CyNetwork getNetworkPointer() {
		return cyNetwork;
	}

	@Override
	public void setNetworkPointer(CyNetwork cyNetwork) {
		this.cyNetwork = cyNetwork;
	}

	@Override
	public Long getSUID() {
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}

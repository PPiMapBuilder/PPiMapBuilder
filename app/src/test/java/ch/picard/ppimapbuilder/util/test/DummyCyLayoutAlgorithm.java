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

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import java.util.Set;

public class DummyCyLayoutAlgorithm implements CyLayoutAlgorithm {
	@Override
	public TaskIterator createTaskIterator(CyNetworkView cyNetworkView, Object o, Set<View<CyNode>> views, String s) {
		return new TaskIterator();
	}

	@Override
	public boolean isReady(CyNetworkView cyNetworkView, Object o, Set<View<CyNode>> views, String s) {
		return false;
	}

	@Override
	public Object createLayoutContext() {
		return null;
	}

	@Override
	public Object getDefaultLayoutContext() {
		return null;
	}

	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		return null;
	}

	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		return null;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}
}

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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

import java.util.Collection;

public class DummyCyNetworkView implements CyNetworkView {
	@Override
	public View<CyNode> getNodeView(CyNode cyNode) {
		return null;
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return null;
	}

	@Override
	public View<CyEdge> getEdgeView(CyEdge cyEdge) {
		return null;
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return null;
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		return null;
	}

	@Override
	public void fitContent() {

	}

	@Override
	public void fitSelected() {

	}

	@Override
	public void updateView() {

	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> visualProperty, V v) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> visualProperty, V v) {

	}

	@Override
	public <T> T getVisualProperty(VisualProperty<T> tVisualProperty) {
		return null;
	}

	@Override
	public boolean isSet(VisualProperty<?> visualProperty) {
		return false;
	}

	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> visualProperty, V v) {

	}

	@Override
	public boolean isValueLocked(VisualProperty<?> visualProperty) {
		return false;
	}

	@Override
	public void clearValueLock(VisualProperty<?> visualProperty) {

	}

	@Override
	public CyNetwork getModel() {
		return null;
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> visualProperty) {
		return false;
	}

	@Override
	public Long getSUID() {
		return null;
	}
}

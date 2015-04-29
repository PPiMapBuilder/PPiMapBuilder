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

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import java.util.HashSet;
import java.util.Set;

public class DummyVisualMappingManager implements VisualMappingManager {
	@Override
	public void setVisualStyle(VisualStyle visualStyle, CyNetworkView cyNetworkView) {

	}

	@Override
	public VisualStyle getVisualStyle(CyNetworkView cyNetworkView) {
		return null;
	}

	@Override
	public Set<VisualStyle> getAllVisualStyles() {
		return new HashSet<VisualStyle>();
	}

	@Override
	public void addVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public void removeVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public VisualStyle getDefaultVisualStyle() {
		return null;
	}

	@Override
	public void setCurrentVisualStyle(VisualStyle visualStyle) {

	}

	@Override
	public VisualStyle getCurrentVisualStyle() {
		return null;
	}

	@Override
	public Set<VisualLexicon> getAllVisualLexicon() {
		return null;
	}
}

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
    
package ch.picard.ppimapbuilder.style;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import ch.picard.ppimapbuilder.data.ontology.goslim.GOSlimRepository;

import java.awt.*;
import java.util.LinkedHashMap;

/**
 * Visual Style definition
 * 
 * @author pidupuis
 *
 */

public class PMBVisualStylesDefinition {
	
	private static PMBVisualStylesDefinition _instance;

	public static PMBVisualStylesDefinition getInstance() {
		if(_instance == null)
			_instance = new PMBVisualStylesDefinition();
		return _instance;
	}
	
	private VisualMappingManager visualMappingManager;
	private VisualMappingFunctionFactory vmfFactoryD;
	private VisualMappingFunctionFactory vmfFactoryP;
	private VisualStyleFactory visualStyleFactoryServiceRef;
	
	private LinkedHashMap<String, VisualStyle> defs = new LinkedHashMap<String, VisualStyle>();
	
	public PMBVisualStylesDefinition setFactories (VisualMappingManager visualMappingManager,
									VisualMappingFunctionFactory vmfFactoryD,
									VisualMappingFunctionFactory vmfFactoryP,
									VisualStyleFactory visualStyleFactoryServiceRef) {
		this.visualMappingManager = visualMappingManager;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		
		return this;
	}
	
	public void addVisualStyle(String name, boolean clustered) {
		// If the style already existed, remove it first
		for (VisualStyle curVS : visualMappingManager.getAllVisualStyles()) {
			if (curVS.getTitle().equalsIgnoreCase(name)) {
				visualMappingManager.removeVisualStyle(curVS);
				break;
			}
		}
		if (defs.containsKey(name)) {
			defs.remove(name);
		}
		
		// Create a new Visual style
		VisualStyle vs = visualStyleFactoryServiceRef.createVisualStyle(name);
		defs.put(name, vs);
		
		initVisualStyleCore(vs);
		if (clustered) {
			initVisualStyleClusterColor(vs);
		}
		
		this.visualMappingManager.addVisualStyle(vs);
	}
	
	
	private void initVisualStyleCore(VisualStyle vs) {
		//NODE
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0, 102, 204)); // Node color
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2.0);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(0, 0, 0));
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 10);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(160, 255, 144));
		PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction("Gene_name", String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(pMapping);
		DiscreteMapping dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Queried", String.class, BasicVisualLexicon.NODE_FILL_COLOR);
		dMapping.putMapValue("true", new Color(255, 255, 51));
		dMapping.putMapValue("false", new Color(0, 102, 204));
		vs.addVisualMappingFunction(dMapping);
		DiscreteMapping dMapping2 = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Queried", String.class, BasicVisualLexicon.NODE_LABEL_COLOR);
		dMapping2.putMapValue("true", new Color(51, 153, 255));
		dMapping2.putMapValue("false", new Color(255, 255, 255));
		vs.addVisualMappingFunction(dMapping2);

		//EDGE
		vs.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(204, 204, 204));
		vs.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, new Color(255, 0, 0));
		dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Interolog", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		dMapping.putMapValue("true", LineTypeVisualProperty.EQUAL_DASH);
		dMapping.putMapValue("false", LineTypeVisualProperty.SOLID);
		vs.addVisualMappingFunction(dMapping);
	}
	

	private void initVisualStyleClusterColor(VisualStyle vs) {
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0, 0, 0)); // Node color
	}
	
	

}

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

import java.awt.*;

/**
 * Visual Style definition
 * 
 * @author pidupuis
 *
 */

public class PMBVisualStyleTask extends AbstractTask {
	
	private final VisualMappingManager visualMappingManager;
	private final VisualMappingFunctionFactory vmfFactoryD;
	private final VisualMappingFunctionFactory vmfFactoryP;
	private final VisualStyleFactory visualStyleFactoryServiceRef;
	
	public PMBVisualStyleTask(VisualMappingManager visualMappingManager, VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryP, VisualStyleFactory visualStyleFactoryServiceRef) {
		this.visualMappingManager = visualMappingManager;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		PMBVisualStylesDefinition.getInstance().addVisualStyle("PPiMapBuilder Visual Style", null); // 'null' because here we do not have applied any layout based on GO
	}
	
	

}

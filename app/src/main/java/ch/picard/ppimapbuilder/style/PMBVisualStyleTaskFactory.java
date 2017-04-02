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

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * PPiMapBuilder task factory for Visual Style
 */
public class PMBVisualStyleTaskFactory extends AbstractTaskFactory {
	
	private final VisualMappingManager visualMappingManager;
	private final VisualMappingFunctionFactory vmfFactoryD;
	private final VisualMappingFunctionFactory vmfFactoryP;
	private final VisualStyleFactory visualStyleFactoryServiceRef;

	public PMBVisualStyleTaskFactory(VisualMappingManager visualMappingManager, VisualMappingFunctionFactory vmfFactoryD, VisualMappingFunctionFactory vmfFactoryP, VisualStyleFactory visualStyleFactoryServiceRef) {
		
		this.visualMappingManager = visualMappingManager;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBVisualStyleTask(visualMappingManager, vmfFactoryD, vmfFactoryP, visualStyleFactoryServiceRef)
		);
	}

}

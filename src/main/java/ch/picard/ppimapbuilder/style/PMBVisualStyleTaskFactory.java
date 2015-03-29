package ch.picard.ppimapbuilder.style;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import ch.picard.ppimapbuilder.ui.credits.CreditFrame;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;

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

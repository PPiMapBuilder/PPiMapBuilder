package ch.picard.ppimapbuilder.style;

import java.awt.Color;

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
		System.out.println("visual style constructor");
		
		this.visualMappingManager = visualMappingManager;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
		
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		System.out.println("visual style task");
		
		//VISUAL STYLE
		// If the style already existed, remove it first
		for (VisualStyle curVS : visualMappingManager.getAllVisualStyles()) {
			if (curVS.getTitle().equalsIgnoreCase("PPiMapBuilder Visual Style")) {
				visualMappingManager.removeVisualStyle(curVS);
				break;
			}
		}

		// Create a new Visual style
		VisualStyle vs = visualStyleFactoryServiceRef.createVisualStyle("PPiMapBuilder Visual Style");

		//NODE
		//vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, vsDefault.getDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR));
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(235, 235, 235)); // Node color
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.5);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(51, 153, 255));
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 10);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(160, 255, 144));
		PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction("Gene_name", String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(pMapping);
		DiscreteMapping dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Queried", String.class, BasicVisualLexicon.NODE_FILL_COLOR);
		dMapping.putMapValue("true", new Color(255, 255, 51));
		dMapping.putMapValue("false", new Color(235, 235, 235));
		vs.addVisualMappingFunction(dMapping);

		//EDGE
		vs.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(204, 204, 204));
		vs.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, new Color(255, 0, 0));
		dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction("Interolog", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		dMapping.putMapValue("true", LineTypeVisualProperty.EQUAL_DASH);
		dMapping.putMapValue("false", LineTypeVisualProperty.SOLID);
		vs.addVisualMappingFunction(dMapping);
		
		visualMappingManager.addVisualStyle(vs);
	}
	
	

}

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
    
package ch.picard.ppimapbuilder.layout;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class PMBGOSlimLayoutTaskFactory implements NetworkViewTaskFactory {
	private CyLayoutAlgorithmManager layoutManager;
	private VisualMappingManager visualMappingManager;

	public PMBGOSlimLayoutTaskFactory(CyLayoutAlgorithmManager layoutManager, VisualMappingManager visualMappingManager) {
		this.layoutManager = layoutManager;
		this.visualMappingManager = visualMappingManager;
	}

	public TaskIterator createTaskIterator(CyNetworkView view) {
		//System.out.println("PMBGOSlimLayoutTaskFactory");
		//System.out.println(view);

		CyNetwork network = view.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();

		if (nodeTable.getColumn("Go_slim") == null) {
			nodeTable.createListColumn("Go_slim", String.class, false);
		}

		return new TaskIterator(
			new PMBGOSlimLayoutTask(network, view, layoutManager, visualMappingManager)
		);
	}

	public boolean isReady(CyNetworkView view) {
		return view != null;
	};
}

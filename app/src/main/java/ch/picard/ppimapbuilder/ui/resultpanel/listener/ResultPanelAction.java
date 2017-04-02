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
    
package ch.picard.ppimapbuilder.ui.resultpanel.listener;

import ch.picard.ppimapbuilder.ui.resultpanel.ResultPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.*;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

import java.util.LinkedHashMap;
import java.util.List;

public class ResultPanelAction implements RowsSetListener {

	private ResultPanel pmbResultPanel;
	private CyApplicationManager cyApplicationManager;
	
	public ResultPanelAction(ResultPanel pmbResultPanel, CyApplicationManager cyApplicationManager) {
		super();
		this.pmbResultPanel = pmbResultPanel;
		this.cyApplicationManager = cyApplicationManager;
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		
		CyNetwork network = this.cyApplicationManager.getCurrentNetwork();

		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		int nbEdgeSelected = 0;
		CyRow myEdgeRow = null;
		
		for (CyEdge edge: selectedEdges) {
			nbEdgeSelected++;
			if (nbEdgeSelected > 1) {
				myEdgeRow = null;
			}
			else {
				myEdgeRow = network.getRow(edge);
			}
		}
		

		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		LinkedHashMap<String, Boolean> clusters = new LinkedHashMap<String, Boolean>();
		
		int nbNodeSelected = 0;
		CyRow myNodeRow = null;
		
		for (CyNode node: selectedNodes) {
			nbNodeSelected++;
			if (nbNodeSelected > 1) {
				myNodeRow = null;
			}
			else {
				myNodeRow = network.getRow(node);
			}
			clusters.put(network.getRow(node).get("Go_slim_group_term", String.class), true);
		}
		
		if (nbNodeSelected == 0) {
			if (nbEdgeSelected == 1) {
				// System.out.println("edge");
				pmbResultPanel.setRow(myEdgeRow);
				pmbResultPanel.setInteractionView(myEdgeRow);
			}
			else if (nbEdgeSelected > 1) {
				// System.out.println("several edges");
				pmbResultPanel.showDefaultView();
			}
			else {
				// System.out.println("nothing");
				pmbResultPanel.showDefaultView();
			}
		}
		else if (nbNodeSelected == 1) {
			// System.out.println("node");
			pmbResultPanel.setRow(myNodeRow);
			pmbResultPanel.setProteinView(myNodeRow);
		}
		else {
			// System.out.println("several nodes");
			
			if (clusters.keySet().size() == 1 && network.getRow(network).get("layout", Boolean.class)) {
				String cl = clusters.keySet().iterator().next();
				pmbResultPanel.setClusterView(cl);
			} else {
				pmbResultPanel.showDefaultView();
			}
		}


	}
}

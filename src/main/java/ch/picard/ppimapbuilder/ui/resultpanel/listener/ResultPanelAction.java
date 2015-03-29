package ch.picard.ppimapbuilder.ui.resultpanel.listener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import ch.picard.ppimapbuilder.ui.resultpanel.ResultPanel;

import java.util.Collection;
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

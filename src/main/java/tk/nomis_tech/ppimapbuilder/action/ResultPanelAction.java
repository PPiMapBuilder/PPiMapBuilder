package tk.nomis_tech.ppimapbuilder.action;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import tk.nomis_tech.ppimapbuilder.ui.resultpanel.ResultPanel;

import java.util.Collection;

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
		// Node selection
		
		if (e.getSource() == this.cyApplicationManager.getCurrentNetwork().getDefaultNodeTable()) {
			System.out.println("Node selected");
			Collection<RowSetRecord> rowsSet = e.getColumnRecords(CyNetwork.SELECTED);

			int nbSelected = 0;
			CyRow myRow = null;

			for (RowSetRecord rowSetRecord : rowsSet) {
				if (rowSetRecord.getRow().get("selected", Boolean.class)) {
					nbSelected++;
					if (nbSelected > 1) {
						myRow = null;
						pmbResultPanel.showDefaultView();
						break;
					}
					myRow = rowSetRecord.getRow();
				}
			}

			if (nbSelected == 1) {
				pmbResultPanel.setProteinView(myRow);
			} else {
				pmbResultPanel.showDefaultView();
			}	
		}
		
		else if (e.getSource() == this.cyApplicationManager.getCurrentNetwork().getDefaultEdgeTable()) {
			System.out.println("Edge selected");
			Collection<RowSetRecord> rowsSet = e.getColumnRecords(CyNetwork.SELECTED);

			int nbSelected = 0;
			CyRow myRow = null;

			for (RowSetRecord rowSetRecord : rowsSet) {
				if (rowSetRecord.getRow().get("selected", Boolean.class)) {
					nbSelected++;
					if (nbSelected > 1) {
						myRow = null;
						pmbResultPanel.showDefaultView();
						break;
					}
					myRow = rowSetRecord.getRow();
				}
			}

			if (nbSelected == 1) {
				pmbResultPanel.setInteractionView(myRow);
			} else {
				pmbResultPanel.showDefaultView();
			}	
		}

	}
}

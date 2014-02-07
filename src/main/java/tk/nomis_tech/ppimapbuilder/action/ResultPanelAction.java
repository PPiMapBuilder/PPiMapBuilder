package tk.nomis_tech.ppimapbuilder.action;

import java.util.Collection;
import java.util.Iterator;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

import tk.nomis_tech.ppimapbuilder.ui.panel.ResultPanel;

public class ResultPanelAction implements RowsSetListener {

	private ResultPanel pmbResultPanel;

	public ResultPanelAction(ResultPanel pmbResultPanel) {
		super();
		this.pmbResultPanel = pmbResultPanel;
	}

	@Override
	public void handleEvent(RowsSetEvent e) {

		Collection<RowSetRecord> rowsSet = e.getColumnRecords(CyNetwork.SELECTED);

		int nbSelected = 0;
		CyRow myRow = null;

		for (Iterator<RowSetRecord> iterator = rowsSet.iterator(); iterator.hasNext();) {
			RowSetRecord rowSetRecord = (RowSetRecord) iterator.next();

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
}

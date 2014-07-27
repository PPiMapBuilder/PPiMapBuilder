package ch.picard.ppimapbuilder.ui.settingwindow.panel.goslim;

import javax.swing.*;

public class GOSlimVisualizer extends JDialog {

	private final JTable table;

	public GOSlimVisualizer() {

		String[][] rows  = new String[3][3];

		Object rowData[][] = { { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
				{ "Row2-Column1", "Row2-Column2", "Row2-Column3"} };
		Object columnNames[] = { "Column One", "Column Two", "Column Three"};
		table = new JTable(rowData, columnNames);
	}


}

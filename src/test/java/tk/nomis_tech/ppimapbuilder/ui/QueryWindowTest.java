package tk.nomis_tech.ppimapbuilder.ui;

import java.util.ArrayList;

import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

/**
 * Manual test for QueryWindow
 */
public class QueryWindowTest {

	public static void main(String[] args) {
		final int size = 40;
		ArrayList<PsicquicService> databases = new ArrayList<PsicquicService>(size);
		
		for(int i = 0; i < size; i++) {
			databases.add(new PsicquicService("Database "+i, null, null, "y", "8", null, null, "y", null));
		}
		
		QueryWindow qw = new QueryWindow();
		qw.updateLists(databases);
		qw.setVisible(true);
	}

}

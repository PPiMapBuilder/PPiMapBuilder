package tk.nomis_tech.ppimapbuilder.ui;

import java.util.ArrayList;

import org.hupo.psi.mi.psicquic.registry.ServiceType;

import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;

/**
 * Manual test for QueryWindow
 */
public class QueryWindowTest {

	public static void main(String[] args) {
		final int size = 40;
		ArrayList<ServiceType> databases = new ArrayList<ServiceType>(size);
		
		for(int i = 0; i < size; i++) {
			ServiceType s = new ServiceType();
			s.getTags().add("ppi");
			s.getTags().add("predicted");
			s.setName("Database "+(i+1));
			databases.add(s);
		}
		
		QueryWindow qw = new QueryWindow();
		qw.updateLists(databases);
		qw.setVisible(true);
	}

}

package tk.nomis_tech.ppimapbuilder;

import java.awt.event.ActionEvent;


import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.result.MitabSearchResult;


/**
 * Creates a new menu item under Apps menu section.
 *
 */
public class MenuAction extends AbstractCyAction {

	private static final long serialVersionUID = 1L;

	public MenuAction(CyApplicationManager cyApplicationManager, final String menuTitle) {
		
		super(menuTitle, cyApplicationManager, null, null);
		setPreferredMenu("Apps");
		
	}

	public void actionPerformed(ActionEvent e) {

		// Write your own function here.
		JOptionPane.showMessageDialog(null, "Hello Cytoscape World!");
		
		UniversalPsicquicClient client = new UniversalPsicquicClient("http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/psicquic");

		MitabSearchResult searchResult = null;
		try {
			searchResult = client.getByQuery("brca2", 0, 200);
		} catch (PsicquicClientException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JOptionPane.showMessageDialog(null, "Interactions: " + searchResult.getTotalCount());
	}
}

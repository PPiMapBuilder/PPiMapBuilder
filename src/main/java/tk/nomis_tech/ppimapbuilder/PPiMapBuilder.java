package tk.nomis_tech.ppimapbuilder;

import javax.swing.JOptionPane;

import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.result.MitabSearchResult;

public class PPiMapBuilder extends AbstractCyApp {

	public PPiMapBuilder(CyAppAdapter adapter) throws PsicquicClientException {
		super(adapter);

		UniversalPsicquicClient client = new UniversalPsicquicClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/psicquic");

		MitabSearchResult searchResult = client.getByQuery(
				"brca2", 0, 200);

		JOptionPane.showMessageDialog(null, "Interactions: " + searchResult.getTotalCount());
	}

}

package ch.picard.ppimapbuilder.layout;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.client.web.QuickGOClient;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PMBGOSlimQueryTask extends AbstractTask {

	private final CyNetwork network;

	public PMBGOSlimQueryTask(CyNetwork network) {
		this.network = network;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		QuickGOClient.GOSlimClient goSlimClient = new QuickGOClient.GOSlimClient();

		monitor.setTitle("Fetching slimmed Gene Ontology");
		System.out.println("PMB Layout Task");

		Set<Protein> networkProteins = new HashSet<Protein>();
		for (CyNode node : network.getNodeList()) {
			networkProteins.add(
					new Protein(
							network.getRow(node).get("uniprot_id", String.class),
							null
					)
			);
		}
		HashMap<Protein, Set<GeneOntologyTerm>> proteinSetHashMap =
				goSlimClient.searchProteinGOSlim(PMBSettings.getInstance().getGoSlimList().get(0), networkProteins);
		//System.out.println(proteinSetHashMap);

		for (CyNode node : network.getNodeList()) {
			final CyRow row = network.getRow(node);
			final Protein protein = new Protein(
					row.get("uniprot_id", String.class),
					null
			);

			if(proteinSetHashMap.containsKey(protein)) {
				ArrayList<String> terms = new ArrayList<String>();
				for(GeneOntologyTerm term : proteinSetHashMap.get(protein))
					terms.add(term.getIdentifier());
				row.set("go_slim", terms);
			}
		}
	}


}

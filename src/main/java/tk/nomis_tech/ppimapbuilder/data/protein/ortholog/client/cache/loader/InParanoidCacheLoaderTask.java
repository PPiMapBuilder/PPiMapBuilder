package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismUtils;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;
import tk.nomis_tech.ppimapbuilder.util.SteppedTaskMonitor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class InParanoidCacheLoaderTask extends AbstractTask {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/OrthoXML/";

	private final List<Organism> organisms;

	protected InParanoidCacheLoaderTask(List<Organism> organisms) {
		this.organisms = organisms;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Set<Pair<Organism>> organismCombination = OrganismUtils.createCombinations(organisms);

		//Set number of steps
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size());
		monitor.setTitle("Loading InParanoid OrthoXML files in cache");

//		XMLInputFactory xmlif = XMLInputFactory.newInstance();

		for (Pair<Organism> organismCouple : organismCombination) {
			Organism organismA = organismCouple.getFirst();
			Organism organismB = organismCouple.getSecond();

			String orthoXMLFile = organismA.getAbbrName() + "/" +
					organismA.getAbbrName() + "-" +
					organismB.getAbbrName() +
					".orthoXML";

			URL u = new URL(BASE_URL + orthoXMLFile);

			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			if (connection.getResponseCode() == 200) {
				monitor.setStep("Loading orthologs between " + organismA + " and " + organismB + "...");

				SpeciesPairProteinOrthologCache cache = PMBProteinOrthologCacheClient.getInstance().getSpeciesPairProteinOrthologCache(organismA, organismB);

				if(!cache.isFull()) {
					//new OrthoXMLParser(xmlif, connection, cache).parse();
					new OrthoXMLParser2(connection, cache).parse();
				}
			}
			connection.disconnect();
		}

		monitor.setProgress(1);
	}

}
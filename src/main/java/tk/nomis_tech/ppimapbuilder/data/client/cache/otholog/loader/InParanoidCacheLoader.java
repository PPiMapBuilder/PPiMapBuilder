package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.loader;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.ProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.SpeciesPairProteinOrthologCache;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.util.SteppedTaskMonitor;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InParanoidCacheLoader extends AbstractTask {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/OrthoXML/";

	private final List<Organism> organisms;

	public InParanoidCacheLoader(List<Organism> organisms) {
		this.organisms = organisms;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		List<Pair<Organism>> organismCombination = new ArrayList<Pair<Organism>>();

		//List all possible combination of organism to get the list of orthoXML files to download and parse
		for (int i = 0, length = organisms.size(); i < length; i++) {
			Organism organismA = organisms.get(i);

			for (int j = i + 1; j < length; j++) {
				Organism organismB = organisms.get(j);

				List<Organism> organismCouple = Arrays.asList(organismA, organismB);
				Collections.sort(organismCouple);

				organismCombination.add(new Pair<Organism>(organismCouple));
			}
		}

		//Set number of steps
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size());
		monitor.setTitle("Loading InParanoid OrthoXML files in cache");

		XMLInputFactory xmlif = XMLInputFactory.newInstance();

		EventFilter filter = new EventFilter() {
			@Override
			public boolean accept(XMLEvent event) {
				return event.isStartElement() || event.isEndElement();
			}
		};

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

				XMLEventReader xmlsr = xmlif.createFilteredReader(
						xmlif.createXMLEventReader(connection.getInputStream()),
						filter
				);

				SpeciesPairProteinOrthologCache cache = ProteinOrthologCacheClient.getInstance().getSpeciesPairProteinOrthologCache(organismA, organismB);
				new OrthoXMLParser(xmlsr, cache).parse();

				xmlsr.close();
			}
			connection.disconnect();
		}

		monitor.setProgress(1);
	}


}

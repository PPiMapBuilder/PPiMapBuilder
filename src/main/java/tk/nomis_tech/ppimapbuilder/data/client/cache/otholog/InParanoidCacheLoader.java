package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.util.SteppedTaskMonitor;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class InParanoidCacheLoader extends AbstractTask {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/OrthoXML/";

	private final List<Organism> organisms;

	private final double minScore = 0.85;

	public InParanoidCacheLoader(List<Organism> organisms) {
		this.organisms = organisms;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		taskMonitor.setTitle("Preparing list of files to load");

		List<Pair<Organism>> organismCombination = new ArrayList<Pair<Organism>>();

		//List all possible combination of organism to get the list of orthoXML files to download and parse
		for (int i = 0, length = organisms.size(); i < length; i++) {
			Organism organismA = organisms.get(i);

			for (int j = i + 1; j < length; j++) {
				Organism organismB = organisms.get(j);

				List<Organism> organismCouple = Arrays.asList(organismA, organismB);
				Collections.sort(organismCouple);
				System.out.println(organismCouple);

				organismCombination.add(new Pair<Organism>(organismCouple));
			}
		}
		
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size());

		//Set number of steps
		monitor.setTitle("Loading OrthoXML files");

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
			System.out.println(u);

			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			if (connection.getResponseCode() == 200) {
				monitor.setStep("Loading orthologs between " + organismA + " and " + organismB + "...");

				XMLEventReader xmlsr = xmlif.createFilteredReader(
						xmlif.createXMLEventReader(connection.getInputStream()),
						filter
				);

				new OrthoXMLParser(xmlsr, organismCouple).parse();
			}
		}
	}

	/**
	 * Parse an orthoXML file directly to the cache
	 */
	class OrthoXMLParser {

		private final XMLEventReader xmlsr;

		private final SpeciesPairProteinOrthologCache.Loader loader;

		public OrthoXMLParser(XMLEventReader xmlsr, Pair<Organism> organismCouple) throws IOException {
			this.xmlsr = xmlsr;
			SpeciesPairProteinOrthologCache cache = ProteinOrthologCacheClient.getInstance()
					.getSpeciesPairProteinOrthologCache(organismCouple.getFirst(), organismCouple.getSecond());
			loader = new SpeciesPairProteinOrthologCache.Loader(cache);
		}

		public void parse() throws XMLStreamException, IOException {
			Organism species = null;
			Map<Integer, Protein> fileProteinIndex = new HashMap<Integer, Protein>();

			ScoredProtein geneRef = null;
			Pair<ScoredProtein> orthologGroup = null;
			List<Pair<? extends Protein>> orthologs = new ArrayList<Pair<? extends Protein>>();

			XMLEvent event;
			while (xmlsr.hasNext()) {
				event = xmlsr.nextEvent();

				if (event.isStartElement()) {
					StartElement element = event.asStartElement();

					// species
					if (is(element, "species")) {
						int taxId = Integer.valueOf(getAttribute(element, "NCBITaxId").getValue());
						species = UserOrganismRepository.getInstance().getOrganismByTaxId(taxId);
						System.out.println();
					}
					// species > gene
					else if (species != null && is(element, "gene")) {
						String protId = getAttribute(element, "protId").getValue();
						Protein protein = new Protein(protId, species);
						int id = Integer.valueOf(getAttribute(element, "id").getValue());

						fileProteinIndex.put(id, protein);
					}


					// orthologGroup
					else if (is(element, "orthologGroup")) {
						orthologGroup = new Pair<ScoredProtein>();
					}
					// orthologGroup > geneRef
					else if (orthologGroup != null && is(element, "geneRef")) {
						int index = Integer.valueOf(getAttribute(element, "id").getValue());
						geneRef = new ScoredProtein(fileProteinIndex.get(index));
					}
					// orthologGroup > geneRef > score
					else if (geneRef != null && is(element, "score")) {
						if (getAttribute(element, "id").getValue().equals("inparalog"))
							geneRef.setScore(Double.valueOf(getAttribute(element, "value").getValue()));
					}

					continue;
				}

				else if (event.isEndElement()) {
					EndElement element = event.asEndElement();

					// species
					if (is(element, "species")) {
						species = null;
					}

					// orthologGroup
					else if (is(element, "orthologGroup")) {
						if (orthologGroup.isComplete()) {
							orthologs.add(orthologGroup);
						}

						orthologGroup = null;
					}
					// orthologGroup > geneRef
					else if (orthologGroup != null && is(element, "geneRef")) {
						if (orthologGroup.isEmpty()) {
							if (geneRef.getScore() >= minScore)
								orthologGroup.setFirst(geneRef);
						} else {
							List<ScoredProtein> prots;
							if (orthologGroup.isComplete())
								prots = Arrays.asList(orthologGroup.getFirst(), orthologGroup.getSecond());
							else
								prots = Arrays.asList(orthologGroup.getFirst());

							boolean inserted = false;
							for (ScoredProtein prot : prots) {
								if (prot.getOrganism().equals(geneRef.getOrganism())) {
									if (geneRef.getScore() > prot.getScore()) {
										orthologGroup.replace(prot, geneRef);
										inserted = true;
									}
								}
							}
							if (!inserted && !orthologGroup.isComplete()) {
								if (geneRef.getScore() >= minScore)
									orthologGroup.setSecond(geneRef);
							}
						}

						geneRef = null;
					}
					continue;
				}
			}
			loader.load(orthologs);
			System.out.println(loader.toString());
		}

		private boolean is(EndElement element, String name) {
			return element.getName().getLocalPart().equals(name);
		}

		private boolean is(StartElement element, String name) {
			return element.getName().getLocalPart().equals(name);
		}

		private Attribute getAttribute(StartElement element, String name) {
			Iterator attributes = element.getAttributes();
			while (attributes.hasNext()) {
				Attribute attribute = (Attribute) attributes.next();

				if (attribute.getName().toString().equals(name))
					return attribute;
			}
			return null;
		}

		class ScoredProtein extends Protein {

			private transient Double score;

			public ScoredProtein(Protein protein) {
				super(protein.getUniProtId(), protein.getOrganism());
			}

			public void setScore(Double score) {
				this.score = score;
			}

			public Double getScore() {
				return score;
			}
		}

	}

}

package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.loader;

import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.client.cache.otholog.SpeciesPairProteinOrthologCache;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.*;

/**
 * Parse an orthoXML file directly into a SpeciesPairProteinOrthologCache
 */
public class OrthoXMLParser {

	private final XMLEventReader xmlsr;

	private final double minScore = 0.85;

	private final SpeciesPairProteinOrthologCache.Loader loader;

	public OrthoXMLParser(XMLEventReader xmlsr, SpeciesPairProteinOrthologCache cache) throws IOException {
		this.xmlsr = xmlsr;
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
					species = OrganismRepository.getInstance().getOrganismByTaxId(taxId);

					if(species == null) {
						String name = getAttribute(element, "name").getValue();
						species = OrganismRepository.getInstance().getOrganismByCommonName(name.trim());
					}
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
package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;

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
import java.util.*;

/**
 * Parse an orthoXML file directly into a SpeciesPairProteinOrthologCache
 */
class OrthoXMLParser {

	private final XMLEventReader xmlsr;

	private final SpeciesPairProteinOrthologCache.Loader loader;

	private static final EventFilter filter = new EventFilter() {
		@Override
		public boolean accept(XMLEvent event) {
			return event.isStartElement() || event.isEndElement();
		}
	};

	public OrthoXMLParser(XMLInputFactory xmlif, HttpURLConnection connection, SpeciesPairProteinOrthologCache cache) throws IOException, XMLStreamException {
		this.xmlsr = xmlif.createFilteredReader(
				xmlif.createXMLEventReader(connection.getInputStream()),
				filter
		);
		loader = cache.newLoader();
	}

	public void parse() throws XMLStreamException, IOException {
		Organism species = null;
		Map<Integer, Protein> fileProteinIndex = new HashMap<Integer, Protein>();

		Integer geneRefID = null;
		Double geneRefScore = null;
		OrthologGroup orthologGroup = null;
		List<OrthologGroup> orthologs = new ArrayList<OrthologGroup>();

		XMLEvent event;
		while (xmlsr.hasNext()) {
			event = xmlsr.nextEvent();

			if (event.isStartElement()) {
				StartElement element = event.asStartElement();

				// species
				if (is(element, "species")) {
					String sTaxId = getAttribute(element, "NCBITaxId").getValue();
					if (sTaxId != null && !sTaxId.isEmpty()) {
						int taxId = Integer.valueOf(sTaxId);
						species = UserOrganismRepository.getInstance().getOrganismByTaxId(taxId);
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
					orthologGroup = new OrthologGroup();
				}
				// orthologGroup > geneRef
				else if (orthologGroup != null && is(element, "geneRef")) {
					geneRefID = Integer.valueOf(getAttribute(element, "id").getValue());
				}
				// orthologGroup > geneRef > score
				else if (geneRefID != null && is(element, "score")) {
					if (getAttribute(element, "id").getValue().equals("inparalog"))
						geneRefScore = Double.valueOf(getAttribute(element, "value").getValue());
				}

				continue;
			} else if (event.isEndElement()) {
				EndElement element = event.asEndElement();

				// species
				if (is(element, "species"))
					species = null;

					// orthologGroup
				else if (is(element, "orthologGroup")) {
					if (orthologGroup.isValid())
						orthologs.add(orthologGroup);

					orthologGroup = null;
				}

				// orthologGroup > geneRef
				else if (orthologGroup != null && is(element, "geneRef")) {
					Protein protein = fileProteinIndex.get(geneRefID);
					if (protein != null) {
						orthologGroup.add(
								new OrthologScoredProtein(
										protein,
										geneRefScore
								)
						);
					}
					geneRefID = null;
					geneRefScore = null;
				}
				continue;
			}
		}
		loader.load(orthologs);
		xmlsr.close();
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

}
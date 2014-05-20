package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import sbc.orthoxml.*;
import sbc.orthoxml.io.OrthoXMLReader;
import tk.nomis_tech.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse an orthoXML file directly into a SpeciesPairProteinOrthologCache
 */
class OrthoXMLParser2 {

	private final OrthoXMLReader orthoXMLReader;

	private final SpeciesPairProteinOrthologCache.Loader loader;

	public OrthoXMLParser2(HttpURLConnection connection, SpeciesPairProteinOrthologCache cache) throws IOException, XMLParseException, XMLStreamException {
		this.orthoXMLReader = new OrthoXMLReader(new InputStreamReader(connection.getInputStream()));
		loader = cache.newLoader();
	}

	public void parse() throws IOException, XMLStreamException {
		List<OrthologGroup> orthologs = new ArrayList<OrthologGroup>();

		Map<String, ScoreDefinition> scoreDefinitions = orthoXMLReader.getScoreDefinitions();
		Map<Integer, Organism> organismMap = new HashMap<Integer, Organism>() {{
			for (Species species : orthoXMLReader.getSpecies()) {
				int tax = species.getNcbiTaxId();
				put(tax, InParanoidOrganismRepository.getInstance().getOrganismByTaxId(tax));
			}
		}};

		Group group;
		while((group = orthoXMLReader.next()) != null) {
			OrthologGroup orthologGroup = new OrthologGroup();

			for (Membership membership : group.getMembers()) {
				Gene gene = membership.getGene();
				Organism organism = organismMap.get(gene.getSpecies().getNcbiTaxId());
				Protein protein = new Protein(gene.getProteinIdentifier(), organism);

				List<Double> inparalogScore = membership.getScores(scoreDefinitions.get("inparalog"));

				orthologGroup.add(protein, inparalogScore.get(0));
			}

			orthologs.add(orthologGroup);
		}

		loader.load(orthologs);
	}

}
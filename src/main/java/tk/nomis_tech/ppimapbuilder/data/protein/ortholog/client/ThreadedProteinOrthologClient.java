package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.List;
import java.util.Map;

public interface ThreadedProteinOrthologClient extends ProteinOrthologClient {

	public Map<Organism, OrthologScoredProtein> getOrthologsMultiOrganism(final Protein protein, final List<Organism> organisms, final Double score) throws Exception;

	public Map<Protein, Map<Organism, OrthologScoredProtein>> getOrthologsMultiOrganismMultiProtein(final List<Protein> proteins, final List<Organism> organisms, final Double score) throws Exception;

}

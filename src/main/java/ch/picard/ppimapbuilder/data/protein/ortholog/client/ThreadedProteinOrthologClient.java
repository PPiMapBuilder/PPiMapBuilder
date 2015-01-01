package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ThreadedProteinOrthologClient extends ProteinOrthologClient {

	public Map<Organism, List<OrthologScoredProtein>> getOrthologsMultiOrganism(final Protein protein, final Collection<Organism> organisms, final Double score) throws Exception;

	public Map<Protein, Map<Organism, List<OrthologScoredProtein>>> getOrthologsMultiOrganismMultiProtein(final Collection<? extends Protein> proteins, final Collection<Organism> organisms, final Double score) throws Exception;

}

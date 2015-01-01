package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.List;

public interface ProteinOrthologClient {

	/**
	 * Gets the orthologous proteins of the given protein in the specified destination organism with a specified
	 * minimum score. Only the top scored orthologs will be returned
	 */
	public List<OrthologScoredProtein> getOrtholog(Protein protein, Organism organism, Double score) throws Exception;

	/**
	 * Gets the {@link OrthologGroup} in which the given protein appear with ortholog of the specified organism
	 */
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception;
}

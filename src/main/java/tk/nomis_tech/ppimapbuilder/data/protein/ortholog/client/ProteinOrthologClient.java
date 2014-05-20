package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

public interface ProteinOrthologClient {

	/**
	 * Gets the orthologous protein of the given protein in the specified destination organism with a specified
	 * minimum score.
	 */
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception;

	/**
	 * Gets the {@link OrthologGroup} in which the given protein appear with ortholog of the specified organism
	 */
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception;
}

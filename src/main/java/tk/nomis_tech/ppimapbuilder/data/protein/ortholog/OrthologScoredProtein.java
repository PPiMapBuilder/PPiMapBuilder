package tk.nomis_tech.ppimapbuilder.data.protein.ortholog;

import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

/**
 * Protein model associated with a score.
 */
public class OrthologScoredProtein extends Protein {

	private final Double score;

	public OrthologScoredProtein(Protein protein, Double score) {
		super(protein.getUniProtId(), protein.getOrganism());
		this.score = score;
	}

	public Double getScore() {
		return score;
	}

}
package tk.nomis_tech.ppimapbuilder.data.protein;

import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;

public class Protein {
	protected final String uniprotId;
	protected final Organism organism;

	public Protein(String uniprotId, Organism organism) {
		this.uniprotId = uniprotId;
		this.organism = organism;
	}

	public Protein(String uniprotId, int taxId) {
		this(uniprotId, OrganismRepository.getInstance().getOrganismByTaxId(taxId));
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public Organism getOrganism() {
		return organism;
	}
}

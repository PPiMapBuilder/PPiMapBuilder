package tk.nomis_tech.ppimapbuilder.data.protein;

import com.eclipsesource.json.JsonObject;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;

/**
 * Simple protein model with only a UniProt id and an Organism
 */
public class Protein {
	protected final String uniProtId;
	protected final Organism organism;

	public Protein(String uniProtId, Organism organism) {
		this.uniProtId = uniProtId;
		this.organism = organism;
	}

	public Protein(String uniProtId, int taxId) {
		this(uniProtId, OrganismRepository.getInstance().getOrganismByTaxId(taxId));
	}

	public String getUniProtId() {
		return uniProtId;
	}

	public Organism getOrganism() {
		return organism;
	}


	@Override
	public String toString() {
		JsonObject out = new JsonObject();
		out.add("uniProtId", uniProtId);
		out.add("organism", organism.toString());
		return out.toString();
	}
}

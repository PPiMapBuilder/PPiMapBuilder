package tk.nomis_tech.ppimapbuilder.data.protein;

import com.eclipsesource.json.JsonObject;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;

import java.io.Serializable;

/**
 * Simple protein model with only a UniProt id and an Organism
 */
public class Protein implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final String uniProtId;
	protected final Organism organism;

	public Protein(Protein protein) {
		this(protein.getUniProtId(), protein.getOrganism());
	}

	public Protein(String uniProtId, Organism organism) {
		this.uniProtId = uniProtId;
		this.organism = organism;
	}

	public Protein(String uniProtId, int taxId) {
		this(uniProtId, UserOrganismRepository.getInstance().getOrganismByTaxId(taxId));
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

	@Override
	public boolean equals(Object o) {
		try {
			return uniProtId.equals(((Protein) o).getUniProtId());
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return uniProtId.hashCode();
	}
}

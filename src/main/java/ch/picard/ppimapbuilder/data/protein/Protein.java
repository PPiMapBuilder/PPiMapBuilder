package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.JSONable;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import com.eclipsesource.json.JsonObject;

import java.io.Serializable;

/**
 * Simple protein model with a unique identifier (UniProt identifier) and an Organism.
 */
public class Protein implements Serializable, JSONable{

	private static final long serialVersionUID = 1L;
	public static final int ID_LENGTH = 10; //MAX length of UniProt identifier

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
		return uniProtId;
	}

	@Override
	public boolean equals(Object o) {
		try {
			return uniProtId.equals(((Protein) o).getUniProtId());
		} catch (Exception e) {
			return o instanceof String && uniProtId.equals((String) o);
		}
	}

	@Override
	public int hashCode() {
		return uniProtId.hashCode();
	}

	@Override
	public String toJSON() {
		JsonObject out = new JsonObject();
		out.add("uniProtId", uniProtId);
		out.add("organism", organism.getTaxId());
		return out.toString();
	}
}

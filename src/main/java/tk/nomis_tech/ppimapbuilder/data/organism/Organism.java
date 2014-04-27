package tk.nomis_tech.ppimapbuilder.data.organism;

import java.io.Serializable;

public class Organism implements Comparable<Organism>, Serializable {

	private final String genus;
	private final String species;
	private final String commonName;
	private final String abbrName;
	private final int ncbiTaxId;

	private Integer inparanoidOrgID;

	protected Organism(String genus, String species, int taxId) {
		this(genus, species, taxId, null);
	}

	protected Organism(String genus, String species, int taxId, Integer inparanoidOrgID) {
		//Format genus name to have a capitalized word
		this.genus = genus.trim().substring(0, 1).toUpperCase() + genus.trim().substring(1).toLowerCase();

		//Format species name to be in lower case
		this.species = species.toLowerCase().trim();

		this.commonName = genus + " " + species;
		this.abbrName = genus.substring(0, 1) + "." + species;

		this.ncbiTaxId = taxId;
		this.inparanoidOrgID = inparanoidOrgID;
	}

	public int getTaxId() {
		return ncbiTaxId;
	}

	public Integer getInparanoidOrgID() {
		return inparanoidOrgID;
	}

	public String getAbbrName() {
		return abbrName;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getGenus() {
		return genus;
	}

	public String getSpecies() {
		return species;
	}

	@Override
	public String toString() {
		return this.getCommonName();
	}

	@Override
	public int compareTo(Organism o) {
		return this.getAbbrName().compareTo(o.getAbbrName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Organism)
			return ncbiTaxId == ((Organism) obj).ncbiTaxId;
		else if (obj instanceof Integer)
			return ncbiTaxId == (Integer) obj;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return ncbiTaxId;
	}
}

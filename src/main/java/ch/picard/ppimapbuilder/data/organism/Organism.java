package ch.picard.ppimapbuilder.data.organism;

import java.io.Serializable;

/**
 * Organism representation inspired by UniProt taxonomy RDF format
 */
public class Organism implements Comparable<Organism>, Serializable {

	private static final long serialVersionUID = 1L;

	private final String scientificName;

	// Scientific name composed of just the genus and species
	private final String simpleScientificName;

	private final String mnemonic;

	// Deduced from scientific name
	private final String genus;
	private final String species;

	private final String commonName;
	private final String abbrName;

	// Unique identifier for Organisms
	private final int ncbiTaxId;

	public Organism(String scientificName, int taxId) {
		this(scientificName, "", "", taxId);
	}

	public Organism(String scientificName, String mnemonic, String commonName, int taxId) {
		String[] parts = scientificName.split(" ");

		// Format genus name to have a capitalized word
		String genus = parts[0];
		this.genus = genus.substring(0, 1).toUpperCase() + genus.substring(1).toLowerCase();

		// Format species name to be in lower case
		String species = parts[1];
		this.species = (!species.equals("sp.")) ? species.toLowerCase() : "";

		this.simpleScientificName = this.genus + " " + this.species;

		// Format scientific name
		this.scientificName = scientificName;

		// Abbreviated name
		this.abbrName =
			!this.species.equals("") ?
				this.genus.substring(0, 1) + "." + this.species
			:
				this.genus + "." + species;

		// Common name
		this.commonName = commonName;

		// Mnemonic name
		this.mnemonic = mnemonic;

		// Identifier
		this.ncbiTaxId = taxId;
	}

	public int getTaxId() {
		return ncbiTaxId;
	}

	public String getScientificName() {
		return scientificName;
	}

	public String getSimpleScientificName() {
		return simpleScientificName;
	}

	public String getAbbrName() {
		return abbrName;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public String getGenus() {
		return genus;
	}

	public String getSpecies() {
		return species;
	}

	@Override
	public String toString() {
		return this.getScientificName();
	}

	@Override
	public int compareTo(Organism o) {
		return this.getAbbrName().compareTo(o.getAbbrName());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		else if (obj instanceof Organism)
			return ncbiTaxId == ((Organism) obj).ncbiTaxId;
		else
			return ncbiTaxId == (Integer) obj;
	}

	public boolean sameSpecies(Organism o) {
		return genus.equals(o.genus) && species.equals(o.species);
	}

	/**
	 * Declaring the NCBI Taxonomy identifier as the unique identifier for all Organisms.
	 * Used in Set, HashSet, HashMap, ... to avoid duplicates.
	 */
	@Override
	public int hashCode() {
		return ncbiTaxId;
	}
}

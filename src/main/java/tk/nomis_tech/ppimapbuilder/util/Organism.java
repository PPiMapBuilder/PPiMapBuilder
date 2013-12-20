package tk.nomis_tech.ppimapbuilder.util;

public class Organism {
	private final String name;
	private final int ncbiTaxId;	
	
	public Organism(String name, int taxId) {
		super();
		this.name = name;
		this.ncbiTaxId = taxId;
	}

	public String getName() {
		return name;
	}

	public int getTaxId() {
		return ncbiTaxId;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	
}

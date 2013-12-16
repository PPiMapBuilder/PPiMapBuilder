package tk.nomis_tech.ppimapbuilder.util;

public class Organism {
	private final String name;
	private final int taxId;
	
	
	
	public Organism(String name, int taxId) {
		super();
		this.name = name;
		this.taxId = taxId;
	}

	public String getName() {
		return name;
	}

	public int getTaxId() {
		return taxId;
	}
	
	
	
	
}

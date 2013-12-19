package tk.nomis_tech.ppimapbuilder.orthology;

/**
 *
 * @author dsi-nomistech
 */
public class Organism {

	private String name;
	private Integer ncbiTaxId;

	public Organism(String name, Integer ncbiTaxId) {
		this.name = name;
		this.ncbiTaxId = ncbiTaxId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNcbiTaxId() {
		return ncbiTaxId;
	}

	public void setNcbiTaxId(Integer ncbiTaxId) {
		this.ncbiTaxId = ncbiTaxId;
	}

}

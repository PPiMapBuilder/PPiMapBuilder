package tk.nomis_tech.ppimapbuilder.data;

public abstract class AbstractProtein {
	protected final String uniprotId;
	protected final Integer taxId;

	protected AbstractProtein(String uniprotId, Integer taxId) {
		this.uniprotId = uniprotId;
		this.taxId = taxId;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public Integer getTaxId() {
		return taxId;
	}
}

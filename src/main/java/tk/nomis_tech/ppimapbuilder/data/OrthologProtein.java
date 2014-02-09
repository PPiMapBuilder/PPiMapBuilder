package tk.nomis_tech.ppimapbuilder.data;

public class OrthologProtein extends AbstractProtein {

	public OrthologProtein(String uniprotId, Integer taxId) {
		super(uniprotId, taxId);
	}

	@Override
	public String toString() {
		return new StringBuilder("{taxid:")
			.append(getTaxId())
			.append(", uniProtId:")
			.append(getUniprotId())
			.append("}").toString();
	}

}

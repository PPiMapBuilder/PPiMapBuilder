package tk.nomis_tech.ppimapbuilder.data.protein;

import com.eclipsesource.json.JsonObject;

public class OrthologProtein extends AbstractProtein {

	public OrthologProtein(String uniprotId, Integer taxId) {
		super(uniprotId, taxId);
	}

	@Override
	public String toString() {
		JsonObject out = new JsonObject();
		out.add("taxId", getTaxId());
		out.add("uniProtId", getUniprotId());
		return out.toString();
	}

}

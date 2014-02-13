package tk.nomis_tech.ppimapbuilder.data;

import com.eclipsesource.json.JsonObject;

public class GeneOntologyModel {

	private String identifier;
	private String term;
	private GOCategory category;

	public GeneOntologyModel(String identifier, String term, GOCategory category) {
		super();
		this.identifier = identifier;
		this.term = term;
		this.category = category;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public GOCategory getCategory() {
		return category;
	}

	public void setCategory(GOCategory category) {
		this.category = category;
	}

	@Override
	public String toString() {
		JsonObject out = new JsonObject();
		out.add("id", identifier);
		out.add("term", term);
		return out.toString();
	}

}

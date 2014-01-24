package tk.nomis_tech.ppimapbuilder.networkbuilder.network.data;

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
		return "{id:" + identifier + ",term:" + term + "category:" + category+"}";
	}

}

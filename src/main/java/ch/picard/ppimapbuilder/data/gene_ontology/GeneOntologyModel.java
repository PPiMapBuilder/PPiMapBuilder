package ch.picard.ppimapbuilder.data.gene_ontology;

import com.eclipsesource.json.JsonObject;

public class GeneOntologyModel {

	private String identifier;
	private String term;
	private GeneOntologyCategory category;

	public GeneOntologyModel(String identifier, String term, GeneOntologyCategory category) {
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

	public GeneOntologyCategory getCategory() {
		return category;
	}

	public void setCategory(GeneOntologyCategory category) {
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

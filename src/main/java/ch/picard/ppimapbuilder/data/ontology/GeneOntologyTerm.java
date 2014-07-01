package ch.picard.ppimapbuilder.data.ontology;

import com.eclipsesource.json.JsonObject;

public class GeneOntologyTerm extends OntologyTerm {

	private final String term;
	private final GeneOntologyCategory category;

	public GeneOntologyTerm(String identifier, String term, char category) {
		this(identifier, term, GeneOntologyCategory.getByLetter(category));
	}

	public GeneOntologyTerm(String identifier, String term, GeneOntologyCategory category) {
		super(identifier);
		this.term = term;
		this.category = category;
	}

	public String getTerm() {
		return term;
	}

	public GeneOntologyCategory getCategory() {
		return category;
	}

	@Override
	public String toString() {
		JsonObject out = new JsonObject();
		out.add("id", getIdentifier());
		out.add("term", term);
		return out.toString();
	}

}

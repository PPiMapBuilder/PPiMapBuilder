package ch.picard.ppimapbuilder.data.ontology;

import ch.picard.ppimapbuilder.data.JSONable;
import com.eclipsesource.json.JsonObject;

public class GeneOntologyTerm extends OntologyTerm implements JSONable {

	private final String term;
	private final GeneOntologyCategory category;
	public static final int TERM_LENGTH = 10;

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
		return getIdentifier();
	}

	@Override
	public String toJSON() {
		JsonObject out = new JsonObject();
		out.add("id", getIdentifier());
		out.add("term", term);
		return out.toString();
	}
}

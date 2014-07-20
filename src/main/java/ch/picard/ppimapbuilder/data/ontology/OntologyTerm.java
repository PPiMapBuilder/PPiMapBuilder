package ch.picard.ppimapbuilder.data.ontology;

import java.io.Serializable;

class OntologyTerm implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String identifier;
	public OntologyTerm(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		try {
			return this.hashCode() == ((OntologyTerm) o).hashCode();
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return getIdentifier();
	}
}


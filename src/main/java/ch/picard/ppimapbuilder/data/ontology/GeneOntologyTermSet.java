package ch.picard.ppimapbuilder.data.ontology;

import java.util.*;

public class GeneOntologyTermSet extends HashSet<GeneOntologyTerm> {

	private static final long serialVersionUID = 1L;

	public GeneOntologyTermSet() {
		super();
	}

	public GeneOntologyTermSet(Collection<GeneOntologyTerm> terms) {
		super(terms);
	}

	public GeneOntologyTermSet getByCategory(GeneOntologyCategory category) {
		GeneOntologyTermSet result = new GeneOntologyTermSet();
		for (GeneOntologyTerm geneOntologyTerm : this) {
			if(geneOntologyTerm.getCategory().equals(category))
				result.add(geneOntologyTerm);
		}
		return result;
	}

	public List<String> asStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this)
			list.add(go.toString());
		return list;
	}
}

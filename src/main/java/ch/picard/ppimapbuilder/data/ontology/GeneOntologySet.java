package ch.picard.ppimapbuilder.data.ontology;

import java.util.Arrays;
import java.util.HashSet;

public class GeneOntologySet extends HashSet<GeneOntologyTerm> {

	private static final long serialVersionUID = 1635830245599971406L;

	private final String name;

	public GeneOntologySet(String name) {
		this.name = name;
	}

	public static GeneOntologySet getDefaultGOslim() {
		GeneOntologySet geneOntologyTerms = new GeneOntologySet("Default GO slim");
		for (String GOTerm : Arrays.asList(
				"GO:0006366", "GO:0001932", "GO:0048666", "GO:0006917", "GO:0016567", "GO:0006916", "GO:0000075",
				"GO:0016570", "GO:0016569", "GO:0006606", "GO:0043161", "GO:0006397", "GO:0000398", "GO:0001525",
				"GO:0055074", "GO:0006354", "GO:0000077", "GO:0030217", "GO:0007088", "GO:0030183", "GO:0006611",
				"GO:0048741", "GO:0006302", "GO:0016571", "GO:0016579", "GO:0006405", "GO:0009452", "GO:0016925",
				"GO:0016574", "GO:0016575", "GO:0016572", "GO:0006282", "GO:0055013", "GO:0000060", "GO:0046323",
				"GO:0007127", "GO:0006306", "GO:0043549", "GO:0043408", "GO:0019083", "GO:0007187", "GO:0002429",
				"GO:0010833", "GO:0055007", "GO:0006007", "GO:0006370", "GO:0006364", "GO:0006475", "GO:0008286",
				"GO:0006633"
		)) {
			geneOntologyTerms.add(new GeneOntologyTerm(GOTerm));
		}
		return geneOntologyTerms;
	}

	public String getName() {
		return name;
	}
}

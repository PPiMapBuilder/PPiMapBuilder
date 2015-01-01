package ch.picard.ppimapbuilder.data.ontology;

import java.io.Serializable;

public enum GeneOntologyCategory implements Serializable {

	MOLECULAR_FUNCTION,
	BIOLOGICAL_PROCESS,
	CELLULAR_COMPONENT;

	public static GeneOntologyCategory getByLetter(char letter) {
		switch (letter) {
			case 'C' : return GeneOntologyCategory.CELLULAR_COMPONENT;
			case 'F' : return GeneOntologyCategory.MOLECULAR_FUNCTION;
			case 'P' : return GeneOntologyCategory.BIOLOGICAL_PROCESS;
			default : return null;
		}
	}

	@Override
	public String toString() {
		switch (this) {
			case MOLECULAR_FUNCTION: return "Molecular function";
			case BIOLOGICAL_PROCESS: return "Biological process";
			case CELLULAR_COMPONENT: return "Cellular component";
		}
		return null;
	}
}

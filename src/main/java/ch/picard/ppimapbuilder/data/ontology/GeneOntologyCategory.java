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

}

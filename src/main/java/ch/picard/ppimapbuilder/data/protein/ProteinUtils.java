package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.ArrayList;
import java.util.Collection;

public class ProteinUtils {

	public static Collection<Protein> newProteins(final Collection<String> identifiers, final Organism organism) {
		return new ArrayList<Protein>(){{
			for(String id : identifiers)
				add(new Protein(id, organism));
		}};
	}

	public static Collection<String> asIdentifiers(final Collection<Protein> proteins) {
		return new ArrayList<String>() {{
			for(Protein protein: proteins)
				add(protein.getUniProtId());
		}};
	}

	/**
	 * @author Kevin Gravouil
	 */
	public static class UniProtId {

		/**
		 * Uniprot ID pattern. According to
		 * http://www.ebi.ac.uk/miriam/main/export/xml/
		 */
		public final static String pattern = "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\.\\d+)?$";

		/**
		 * Test if a given string matches the Uniprot ID pattern.
		 *
		 * @param uniprotId
		 * @return boolean
		 */
		public static boolean isValid(String uniprotId) {
			return uniprotId.matches(pattern);
		}

	}
}

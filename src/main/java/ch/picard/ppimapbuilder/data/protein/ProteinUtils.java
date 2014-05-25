package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		public final static Pattern pattern = Pattern.compile("([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(\\-\\d+)?");

		/**
		 * Test if a given string matches the Uniprot ID pattern.
		 *
		 * @param uniprotId
		 * @return boolean
		 */
		public static boolean isValid(String uniprotId) {
			Matcher matcher = pattern.matcher(uniprotId);
			boolean matches = matcher.matches();
			return matches;
		}

	}
}

package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProteinUtils {

	public static Set<Protein> newProteins(final Set<String> identifiers, final Organism organism) {
		final Set<Protein> out = new HashSet<Protein>();
		for(String id : identifiers)
			out.add(new Protein(id, organism));
		return out;
	}

	public static Collection<String> asIdentifiers(final Set<Protein> proteins) {
		final Set<String> out = new HashSet<String>();
		for(Protein protein: proteins)
			out.add(protein.getUniProtId());
		return out;
	}

	/**
	 * @author Kevin Gravouil
	 */
	public static class UniProtId {

		/**
		 * Uniprot ID pattern. According to
		 * http://www.ebi.ac.uk/miriam/main/export/xml/
		 */
		private final static Pattern pattern = Pattern.compile("([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(\\-\\d+)?");
		private final static Pattern strictPattern = Pattern.compile("([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})");

		/**
		 * Test if a given string matches the Uniprot ID pattern.
		 *
		 * @param uniprotId
		 * @return boolean
		 */
		public static boolean isValid(String uniprotId) {
			Matcher matcher = pattern.matcher(uniprotId);
			return matcher.matches();
		}

		/**
		 * Extract strict UniProt ID from a string
		 */
		public static String extractStrictUniProtId(String string) {
			Matcher matcher = strictPattern.matcher(string);
			if(matcher.matches())
				return matcher.group();
			return  null;
		}

	}
}

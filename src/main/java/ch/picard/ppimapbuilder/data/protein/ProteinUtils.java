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
	 * Making sure a protein entry is from a specific organism. Will change the entry organism if the entry don't have
	 * exactly the same organism as specified but is from the same species (differences can come from strains).
	 */
	public static UniProtEntry correctEntryOrganism(UniProtEntry originalEntry, Organism organism) {
		if (
			!originalEntry.getOrganism().equals(organism) &&
			originalEntry.getOrganism().sameSpecies(organism)
		) {
			return new UniProtEntry.Builder(originalEntry)
					.setOrganism(organism)
					.build();
		}
		return originalEntry;
	}

	/**
	 * @author Kevin Gravouil
	 */
	public static class UniProtId {

		/**
		 * Uniprot ID pattern. According to
		 * http://www.ebi.ac.uk/miriam/main/export/xml/
		 */
		private final static Pattern pattern = Pattern.compile("([OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2})(\\-\\w+)?");

		/**
		 * Test if a given string matches the Uniprot ID pattern (authorizing extension like "-1" or "-PRO_XXXXXX").
		 */
		public static boolean isValid(String uniprotId) {
			Matcher matcher = pattern.matcher(uniprotId);
			return matcher.matches();
		}

		/**
		 * Test if a given string matches the strict Uniprot ID pattern (no extension authorized)
		 */
		public static boolean isStrict(String uniprotId) {
			Matcher matcher = pattern.matcher(uniprotId);
			return matcher.matches() && uniprotId.equals(matcher.group(1));
		}

		/**
		 * Extract strict UniProt ID from a string
		 */
		public static String extractStrictUniProtId(String string) {
			Matcher matcher = pattern.matcher(string);
			if(matcher.matches())
				return matcher.group(1);
			return  null;
		}

	}
}

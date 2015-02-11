package ch.picard.ppimapbuilder.data.organism;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.PairUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganismUtils {

	private static final Pattern pattern = Pattern.compile("\\(.*\\)");

	/**
	 * Creates pair combination of organisms using an organism list.
	 */
	public static Set<Pair<Organism>> createCombinations(List<Organism> organisms) {
		return PairUtils.createCombinations(new HashSet<Organism>(organisms), false, true);
	}

	public static List<String> organismsToStrings(Collection<Organism> organisms) {
		ArrayList<String> out = new ArrayList<String>();
		for(Organism organism : organisms) {
			out.add(organism.getScientificName());
		}
		return out;
	}

	public static Organism findOrganismInMITABTaxId(OrganismRepository repository, String taxId) {
		Matcher matcher;
		int taxIdA = Integer.parseInt(taxId);
		Organism organism = repository.getOrganismByTaxId(taxIdA);

		if(organism == null && (matcher = pattern.matcher(taxId)).matches()) {
			String[] genusSpecies = matcher.group(1).split(" ");
			organism = InParanoidOrganismRepository.getInstance().getOrganismByGenusAndSpecies(
					genusSpecies[0],
					genusSpecies[1]
			);
		}
		return organism;
	}

}

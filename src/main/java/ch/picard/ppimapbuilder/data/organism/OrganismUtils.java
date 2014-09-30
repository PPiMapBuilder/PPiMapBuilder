package ch.picard.ppimapbuilder.data.organism;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.PairUtils;

import java.util.*;

public class OrganismUtils {

	/**
	 * Creates pair combination of organisms using an organism list.
	 */
	public static Set<Pair<Organism>> createCombinations(List<Organism> organisms) {
		return PairUtils.createCombinations(new HashSet<Organism>(organisms), false, true);
	}


	public static List<String> organismsToStrings(List<Organism> organisms) {
		ArrayList<String> out = new ArrayList<String>();
		for(Organism organism : organisms) {
			out.add(organism.getScientificName());
		}
		return out;
	}
}

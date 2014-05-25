package ch.picard.ppimapbuilder.data.organism;

import ch.picard.ppimapbuilder.data.Pair;

import java.util.*;

public class OrganismUtils {

	/**
	 * Creates pair combination of organisms using an organism list.
	 */
	public static Set<Pair<Organism>> createCombinations(List<Organism> organisms) {
		final Set<Pair<Organism>> organismCombination = new HashSet<Pair<Organism>>();

		//List all possible combination of organism to get the list of orthoXML files to download and parse
		Organism organismA, organismB;
		for (int i = 0, length = organisms.size(); i < length; i++) {
			organismA = organisms.get(i);

			for (int j = i + 1; j < length; j++) {
				organismB = organisms.get(j);

				List<Organism> organismCouple = Arrays.asList(organismA, organismB);
				Collections.sort(organismCouple);

				organismCombination.add(new Pair<Organism>(organismCouple));
			}
		}

		return organismCombination;
	}
}

package ch.picard.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PairUtils {

	/**
	 * Creates pair combination of a Set of elements
	 * @param elements Set of elements to combine to one an other
	 * @param selfCombination if true, pairs of duplicate will be added in the list of combinations
	 * @param orderedPairs if true and if the elements are Comparable, the combination pairs will be ascending ordered
	 */
	public static <T> Set<Pair<T>> createCombinations(Set<T> elements, boolean selfCombination, boolean orderedPairs) {
		final ArrayList<T> list = new ArrayList<T>(elements);

		T A, B;
		final Set<Pair<T>> combinations = new HashSet<Pair<T>>();
		for (int i = 0, length = list.size(); i < length; i++) {
			A = list.get(i);

			if(selfCombination)
				combinations.add(new Pair<T>(A, A));

			for (int j = i + 1; j < length; j++) {
				B = list.get(j);

				if(orderedPairs && A instanceof Comparable && ((Comparable<T>) A).compareTo(B) > 0)
					combinations.add(new Pair<T>(B, A));
				else
					combinations.add(new Pair<T>(A, B));
			}
		}

		return combinations;
	}

}

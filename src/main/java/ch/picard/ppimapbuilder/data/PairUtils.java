/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
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

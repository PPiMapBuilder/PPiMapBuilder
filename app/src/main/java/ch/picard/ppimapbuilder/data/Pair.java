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

import com.google.common.base.Objects;

import java.util.List;

/**
 * Convenient class to store object by pair
 */
public class Pair<T> implements Comparable<Pair<T>> {

	private final T first;
	private final T second;

	public Pair(List<T> elems) {
		this(elems.get(0), elems.get(1));
	}

	public Pair(T first, T second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public boolean isNotNull() {
		return first != null && second != null;
	}

	public boolean isEmpty() {
		return first == null && second == null;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(first.hashCode(), -1, second.hashCode());
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Pair){
			Pair otherPair = (Pair) other;

			return (
					(otherPair.first == null && first == null) ||
					(otherPair.first != null && otherPair.first.equals(first))
				)
					&&
				(
					(otherPair.second == null && second == null) ||
					(otherPair.second != null && otherPair.second.equals(second))
				);
		}
		else return false;
	}

	@Override
	public String toString() {
		return "Pair["+first.toString()+", "+second.toString()+"]";
	}

	@Override
	public int compareTo(Pair<T> o) {
		return toString().compareTo(o.toString());
	}
}

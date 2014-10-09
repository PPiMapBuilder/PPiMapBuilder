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

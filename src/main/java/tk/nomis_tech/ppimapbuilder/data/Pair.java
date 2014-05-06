package tk.nomis_tech.ppimapbuilder.data;

import java.util.List;

/**
 * Convenient class to store object by pair
 */
public class Pair<T> {

	private T first;
	private T second;

	public Pair(List<T> elems) {
		this(elems.get(0), elems.get(1));
	}

	public Pair(T first, T second) {
		this.first = first;
		this.second = second;
	}

	public Pair() {}

	public void setFirst(T first) {
		this.first = first;
	}

	public void setSecond(T second) {
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public boolean isComplete() {
		return first != null && second != null;
	}

	public boolean isEmpty() {
		return first == null && second == null;
	}

	public void replace(T oldElement, T newElement) {
		if(oldElement == first)
			first = newElement;
		else if(oldElement == second)
			second = newElement;
	}

	@Override
	public String toString() {
		return "Pair["+first.toString()+", "+second.toString()+"]";
	}
}

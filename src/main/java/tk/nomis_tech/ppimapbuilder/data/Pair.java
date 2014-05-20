package tk.nomis_tech.ppimapbuilder.data;

import com.google.common.base.Objects;

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
	public int hashCode() {
		int f = first.hashCode(), s = second.hashCode();
		int a = Math.max(f, s), b = Math.min(f, s);
		//return Integer.parseInt(a + "" + b);
		return Objects.hashCode(a, b);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Pair)
			return ((Pair) obj).hashCode() == this.hashCode();
		else
			return false;
	}

	@Override
	public String toString() {
		int f = first.hashCode(), s = second.hashCode();
		int a = Math.max(f, s), b = Math.min(f, s);
		//return "Pair["+first.toString()+", "+second.toString()+"]";
		return "Pair["+a+", "+b+"]";
	}
}

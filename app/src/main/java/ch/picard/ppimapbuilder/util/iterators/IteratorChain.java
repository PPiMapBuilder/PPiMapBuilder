package ch.picard.ppimapbuilder.util.iterators;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An IteratorChain is an Iterator that wraps a number of Iterators.
 * <p/>
 * Most of this implementation comes from apaches collections 4s
 */
public class IteratorChain<E> implements Iterator<E> {

	/**
	 * The chain of iterators
	 */
	private final Queue<Iterator<? extends E>> iteratorChain = new LinkedList<Iterator<? extends E>>();

	/**
	 * The current iterator
	 */
	private Iterator<? extends E> currentIterator = null;

	/**
	 * Construct an IteratorChain with no Iterators.
	 * <p/>
	 * You will normally use {@link #addIterator(Iterator)} to add some
	 * iterators after using this constructor.
	 */
	public IteratorChain() {
		super();
	}

	/**
	 * Add an Iterator to the end of the chain
	 *
	 * @param iterator Iterator to add
	 * @throws IllegalStateException if I've already started iterating
	 * @throws NullPointerException  if the iterator is null
	 */
	public void addIterator(final Iterator<? extends E> iterator) {
		iteratorChain.add(iterator);
	}

	/**
	 * Returns the remaining number of Iterators in the current IteratorChain.
	 *
	 * @return Iterator count
	 */
	public int size() {
		return iteratorChain.size();
	}

	/**
	 * Updates the current iterator field to ensure that the current Iterator is
	 * not exhausted
	 */
	protected void updateCurrentIterator() {
		if (currentIterator == null) {
			if (iteratorChain.isEmpty()) {
				currentIterator = new EmptyIterator<E>();
			} else {
				currentIterator = iteratorChain.remove();
			}
			// set last used iterator here, in case the user calls remove
			// before calling hasNext() or next() (although they shouldn't)
		}

		while (currentIterator.hasNext() == false && !iteratorChain.isEmpty()) {
			currentIterator = iteratorChain.remove();
		}
	}

	/**
	 * Return true if any Iterator in the IteratorChain has a remaining element.
	 *
	 * @return true if elements remain
	 */
	public boolean hasNext() {
		updateCurrentIterator();
		return currentIterator.hasNext();
	}

	/**
	 * Returns the next Object of the current Iterator
	 *
	 * @return Object from the current Iterator
	 * @throws java.util.NoSuchElementException if all the Iterators are
	 *                                          exhausted
	 */
	public E next() {
		updateCurrentIterator();
		return currentIterator.next();
	}

	@Override
	public void remove() {}

}

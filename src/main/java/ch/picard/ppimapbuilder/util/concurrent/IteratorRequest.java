package ch.picard.ppimapbuilder.util.concurrent;

import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Alias to Callable<Iterator<T>>
 * @param <T>
 */
public interface IteratorRequest<T> extends Callable<Iterator<T>> {}

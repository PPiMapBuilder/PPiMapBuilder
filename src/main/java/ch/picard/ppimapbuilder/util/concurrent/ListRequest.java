package ch.picard.ppimapbuilder.util.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Alias to Callable<List<T>>
 * @param <T>
 */
public interface ListRequest<T> extends Callable<List<T>> {}

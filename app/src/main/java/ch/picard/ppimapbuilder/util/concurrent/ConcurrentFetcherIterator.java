package ch.picard.ppimapbuilder.util.concurrent;

import ch.picard.ppimapbuilder.util.iterators.IteratorChain;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * an Iterator producing a "stream" of object from a list of concurrent iterator requests
 * @param <T>
 */
public class ConcurrentFetcherIterator<T> implements Iterator<T> {

	private boolean canceled = false;
	private final IteratorChain<T> innerIterator;
	private final ExecutorService threadPool;
	private final Thread feedingThread;

	public ConcurrentFetcherIterator(
			final List<? extends IteratorRequest<T>> requests,
			ExecutorServiceManager executorServiceManager
	) {
		this.innerIterator = new IteratorChain<T>();
		this.threadPool = executorServiceManager.getOrCreateThreadPool();

		// Daemon thread feeding the iterator with objects
		feedingThread = new Thread(new ConcurrentExecutor<Iterator<T>>(threadPool, requests.size()) {

			@Override
			public Callable<Iterator<T>> submitRequests(int index) {
				return requests.get(index);
			}

			@Override
			public void processResult(Iterator<T> intermediaryResult, Integer index) {
				getInnerIterator().addIterator(intermediaryResult);
			}
		});
		feedingThread.setDaemon(true);
		feedingThread.start();
	}

	private synchronized IteratorChain<T> getInnerIterator() {
		return innerIterator;
	}

	@Override
	public boolean hasNext() {
		return !canceled && innerCouldHaveNext();
	}

	@Override
	public T next() {
		if (canceled || !innerCouldHaveNext()) return null;
		if (!getInnerIterator().hasNext()) innerWaitForNext();
		return getInnerIterator().next();
	}

	@Override
	public void remove() {}

	/**
	 * Wait for thread pool to terminate or shutdown if no next element is available
	 */
	private void innerWaitForNext() {
		try {
			while(!canceled && !getInnerIterator().hasNext() && innerCouldHaveNext() && !awaitTermination());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean awaitTermination() throws InterruptedException {
		return threadPool.awaitTermination(500l, TimeUnit.MILLISECONDS);
	}

	private boolean innerCouldHaveNext() {
		return getInnerIterator().hasNext() || (!threadPool.isTerminated() && !threadPool.isShutdown() && feedingThread.isAlive());
	}

	public void cancel() {
		canceled = true;
		if(feedingThread.isAlive())
			feedingThread.interrupt();
		threadPool.shutdown();
	}
}

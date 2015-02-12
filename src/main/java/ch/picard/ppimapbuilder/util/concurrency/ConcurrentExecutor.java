package ch.picard.ppimapbuilder.util.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class ConcurrentExecutor<R> implements Runnable {

	private final ExecutorService executorService;
	private final int nbRequests;
	private final ExecutorServiceManager executorServiceManager;
	private boolean cancel = false;

	private ConcurrentExecutor(ExecutorService executorService, ExecutorServiceManager executorServiceManager, int nbRequests) {
		this.executorService = executorService == null ? executorServiceManager.getOrCreateThreadPool() : executorService;
		this.nbRequests = nbRequests;
		this.executorServiceManager = executorServiceManager;
	}

	/**
	 * Constructs a new ConcurrentExecutor with an executorServiceManager that will be use to create a new ExecutorService
	 * @param executorServiceManager an executorServiceManager
	 * @param nbRequests number of request that will be launched
	 */
	public ConcurrentExecutor(ExecutorServiceManager executorServiceManager, int nbRequests) {
		this(null, executorServiceManager, nbRequests);
	}

	/**
	 * Constructs a new ConcurrentExecutor with an executorService in which requests will be submitted
	 * @param executorService an executorService
	 * @param nbRequests number of request that will be launched
	 */
	public ConcurrentExecutor(ExecutorService executorService, int nbRequests) {
		this(executorService, null, nbRequests);
	}

	/**
	 * Launches all requests
	 */
	@Override
	public final void run() {
		if (executorService == null || nbRequests <= 1 || (executorServiceManager != null && executorServiceManager.getMaxNumberThread() <= 1)) {
			// Fallback without concurrent requests
			for (int i = 0; i < nbRequests; i++) {
				if(cancel) return;
				try {
					processResult(submitRequests(i).call(), i);
				} catch (Exception e) {
					if (processExecutionException(new ExecutionException(e), i))
						break;
				}
			}
		} else {
			//Initializations
			final Map<Future<R>, Integer> futuresIndexed = new HashMap<Future<R>, Integer>(nbRequests);
			final CompletionService<R> completionService = new ExecutorCompletionService<R>(executorService);

			//Submit requests
			for (int i = 0; i < nbRequests; i++) {
				if(cancel) break;
				Callable<R> callable = submitRequests(i);
				if(callable != null)
					futuresIndexed.put(completionService.submit(callable), i);

				// Wait a bit between first requests
				if (executorServiceManager != null && executorServiceManager.getMaxNumberThread() >= i - 1) {
					try {
						Thread.sleep(100);
					} catch (Exception ignore) {}
				}
			}

			if(cancel) {
				executorService.shutdown();
				return;
			}

			//Process results and errors
			for (Future<R> ignored : futuresIndexed.keySet()) {
				Future<R> take = null;
				Integer index = null;
				try {
					if (cancel)
						executorService.shutdown();
					if (executorService.isShutdown())
						break;

					take = completionService.take();
					index = futuresIndexed.get(take);

					R result = take.get();

					if (result != null)
						processResult(result, index);
				} catch (InterruptedException e) {
					if (processInterruptedException(e))
						break;
				} catch (ExecutionException e) {
					if (processExecutionException(e, index))
						break;
				}
			}
		}
		if(executorServiceManager != null) executorServiceManager.unRegister(executorService);
	}

	/**
	 * Method to implement in order to create new requests
	 * @param index index of the current request to be created
	 * @return the request
	 */
	public abstract Callable<R> submitRequests(int index);

	/**
	 * Method to implement in order to process result of requests.
	 * @param result the result of a request obtained
	 * @param index the index of the request from which this result comes
	 */
	public void processResult(R result, Integer index) {}

	/**
	 * Method to override in order to process InterruptedException occurring during a request
	 * @return if true, the ConcurrentExecutor will stop fetching other request responses
	 */
	public boolean processInterruptedException(InterruptedException e) {
		e.printStackTrace();
		return false;
	}

	/**
	 * Method to override in order to process ExecutionException occurring during a request.
	 * An ExecutionException has a cause obtainable with ExecutionException.getCause().
	 * @return if true, the ConcurrentExecutor will stop fetching other request responses
	 */
	public boolean processExecutionException(ExecutionException e, Integer index) {
		e.printStackTrace();
		return false;
	}

	public void cancel() {
		this.cancel = true;
	}
}

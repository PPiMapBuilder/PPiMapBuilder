package ch.picard.ppimapbuilder.util.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class ConcurrentExecutor<R> implements Runnable {

	private final ExecutorService executorService;
	private final int nbRequests;
	private final ExecutorServiceManager executorServiceManager;

	private ConcurrentExecutor(ExecutorService executorService, ExecutorServiceManager executorServiceManager, int nbRequests) {
		this.executorService = executorService == null ? executorServiceManager.getOrCreateThreadPool() : executorService;
		this.nbRequests = nbRequests;
		this.executorServiceManager = executorServiceManager;
	}

	public ConcurrentExecutor(ExecutorServiceManager executorServiceManager, int nbRequests) {
		this(null, executorServiceManager, nbRequests);
	}

	public ConcurrentExecutor(ExecutorService executorService, int nbRequests) {
		this(executorService, null, nbRequests);
	}

	@Override
	public final void run() {
		if (executorService == null || nbRequests <= 1) {
			for (int i = 0; i < nbRequests; i++) {
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
				Callable<R> callable = submitRequests(i);
				if(callable != null)
					futuresIndexed.put(completionService.submit(callable), i);
			}

			//Process results and errors
			for (Future<R> ignored : futuresIndexed.keySet()) {
				Future<R> take = null;
				Integer index = null;
				try {
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

	public abstract Callable<R> submitRequests(int index);

	public void processResult(R result, Integer index) {}

	public boolean processInterruptedException(InterruptedException e) {
		e.printStackTrace();
		return false;
	}

	public boolean processExecutionException(ExecutionException e, Integer index) {
		e.printStackTrace();
		return false;
	}
}

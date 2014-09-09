package ch.picard.ppimapbuilder.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public abstract class ConcurrentExecutor<R> implements Runnable {

	private final ExecutorService executorService;
	private final int nbRequests;

	public ConcurrentExecutor(ExecutorService executorService, int nbRequests) {
		this.executorService = executorService;
		this.nbRequests = nbRequests;
	}

	@Override
	public void run() {
		//Initializations
		final Map<Future<R>, Integer> results = new HashMap<Future<R>, Integer>(nbRequests);
		final CompletionService<R> completionService = new ExecutorCompletionService<R>(executorService);

		//Submit requests
		for(int i = 0; i < nbRequests; i++)
			results.put(completionService.submit(submitRequests(i)), i);

		//Process results and errors
		for (Future<R> ignored : results.keySet()) {
			Future<R> take = null;
			try {
				if (executorService.isShutdown())
					break;

				take = completionService.take();
				R result = take.get();

				if (result != null)
					processResult(result, results.get(take));
			} catch (InterruptedException e) {
				if (processInterruptedException(e))
					break;
			} catch (ExecutionException e) {
				if (processExecutionException(e))
					break;
			}
		}
	}

	public abstract Callable<R> submitRequests(int index);

	public abstract void processResult(R result, Integer index);

	public boolean processInterruptedException(InterruptedException e) {
		e.printStackTrace();
		return false;
	}

	public boolean processExecutionException(ExecutionException e) {
		e.printStackTrace();
		return false;
	}
}

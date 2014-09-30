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
		final Map<Future<R>, Integer> futuresIndexed = new HashMap<Future<R>, Integer>(nbRequests);
		final CompletionService<R> completionService = new ExecutorCompletionService<R>(executorService);

		//Submit requests
		for(int i = 0; i < nbRequests; i++)
			futuresIndexed.put(completionService.submit(submitRequests(i)), i);

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

	public abstract Callable<R> submitRequests(int index);

	public abstract void processResult(R result, Integer index);

	public boolean processInterruptedException(InterruptedException e) {
		e.printStackTrace();
		return false;
	}

	public boolean processExecutionException(ExecutionException e, Integer index) {
		e.printStackTrace();
		return false;
	}
}

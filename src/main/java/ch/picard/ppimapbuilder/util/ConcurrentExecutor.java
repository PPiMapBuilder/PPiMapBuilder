package ch.picard.ppimapbuilder.util;

import java.util.ArrayList;
import java.util.List;
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
		final List<Future<R>> results = new ArrayList<Future<R>>(nbRequests);
		final CompletionService<R> completionService = new ExecutorCompletionService<R>(executorService);

		//Submit requests
		for(int i = 0; i < nbRequests; i++)
			results.add(completionService.submit(submitRequests(i)));

		//Process results and errors
		for (int i = 0, requestsSize = results.size(); i < requestsSize; i++) {
			Future<R> take = null;
			try {
				take = completionService.take();
				R result = take.get();

				if (result != null)
					processResult(result);
			} catch (InterruptedException e) {
				if(!processInterruptedException(e))
					continue;
				else break;
			} catch (ExecutionException e) {
				if(!processExecutionException(e))
					continue;
				else break;
			}
		}
	}

	public abstract Callable<R> submitRequests(int index);

	public abstract void processResult(R result);

	public boolean processInterruptedException(InterruptedException e) {
		e.printStackTrace();
		return false;
	}

	public boolean processExecutionException(ExecutionException e) {
		e.printStackTrace();
		return false;
	}
}

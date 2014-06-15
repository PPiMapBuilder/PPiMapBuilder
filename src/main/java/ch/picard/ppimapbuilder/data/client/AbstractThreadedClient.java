package ch.picard.ppimapbuilder.data.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract class providing base element for threaded web service clients.
 */
public abstract class AbstractThreadedClient {
	protected Integer maxNumberThread;
	private ExecutorService executorService;

	public AbstractThreadedClient(Integer maxNumberThread) {
		this.maxNumberThread = maxNumberThread;
	}

	public AbstractThreadedClient() {
		this(null);
	}

	public ExecutorService setExecutorService(ExecutorService executorService) {
		return this.executorService = executorService;
	}

	public ExecutorService newFixedThreadPool() {
		if (executorService == null) {
			executorService = maxNumberThread != null ?
					Executors.newFixedThreadPool(maxNumberThread) :
					Executors.newCachedThreadPool();
		}
		return executorService;
	}
}

package ch.picard.ppimapbuilder.data.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract class providing base element for threaded web service clients.
 */
public abstract class AbstractThreadedClient {
	protected Integer maxNumberThread;
	private List<ExecutorService> executorServices;

	public AbstractThreadedClient(Integer maxNumberThread) {
		this.executorServices = new ArrayList<ExecutorService>();
		this.maxNumberThread = maxNumberThread;
	}

	public AbstractThreadedClient() {
		this(null);
	}

	public void setMaxNumberThread(Integer maxNumberThread) {
		this.maxNumberThread = maxNumberThread;
	}

	public List<ExecutorService> getExecutorServices() {
		return this.executorServices;
	}

	public ExecutorService newThreadPool() {
		ExecutorService executorService = maxNumberThread != null ?
				Executors.newFixedThreadPool(maxNumberThread) :
				Executors.newCachedThreadPool();
		executorServices.add(executorService);
		return executorService;
	}

}

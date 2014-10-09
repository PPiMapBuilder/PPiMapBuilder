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
	private final List<ExecutorService> executorServices;

	public AbstractThreadedClient(Integer maxNumberThread) {
		this.executorServices = new ArrayList<ExecutorService>();
		this.maxNumberThread = maxNumberThread;
	}

	public List<ExecutorService> getExecutorServices() {
		return this.executorServices;
	}

	protected ExecutorService newThreadPool() {
		ExecutorService executorService = maxNumberThread != null ?
				newThreadPool(maxNumberThread) :
				null;
		return executorService;
	}

	protected ExecutorService newThreadPool(int nbThread) {
		ExecutorService executorService = Executors.newFixedThreadPool(nbThread);
		executorServices.add(executorService);
		return executorService;
	}

}

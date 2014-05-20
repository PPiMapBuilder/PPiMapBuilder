package tk.nomis_tech.ppimapbuilder.data.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Abstract class providing base element for threaded web service clients.
 */
public abstract class AbstractThreadedClient {
	protected int maxNumberThread;
	protected ThreadFactory threadFactory;

	public AbstractThreadedClient(int maxNumberThread) {
		this.maxNumberThread = maxNumberThread;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public ExecutorService newFixedThreadPool() {
		return threadFactory != null ?
				Executors.newFixedThreadPool(maxNumberThread, threadFactory) :
				Executors.newFixedThreadPool(maxNumberThread);
	}
}

package tk.nomis_tech.ppimapbuilder.data.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractThreadedClient {
	protected int nThread;
	protected ThreadFactory threadFactory;

	public AbstractThreadedClient(int nThread) {
		this.nThread = nThread;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public ExecutorService newFixedThreadPool() {
		return threadFactory != null ?
				Executors.newFixedThreadPool(nThread, threadFactory) :
				Executors.newFixedThreadPool(nThread);
	}
}

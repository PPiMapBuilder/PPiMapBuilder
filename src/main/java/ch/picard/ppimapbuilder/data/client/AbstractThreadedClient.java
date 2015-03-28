package ch.picard.ppimapbuilder.data.client;

import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;

import java.util.concurrent.ExecutorService;

/**
 * Abstract class providing base element for threaded web service clients.
 */
public abstract class AbstractThreadedClient {

    private final ExecutorServiceManager executorServiceManager;

    protected AbstractThreadedClient(ExecutorServiceManager executorServiceManager) {
        this.executorServiceManager = executorServiceManager;
    }

    public ExecutorService getOrCreateThreadPool() {
        return executorServiceManager.getOrCreateThreadPool();
    }

    public void unRegister(ExecutorService service) {
        executorServiceManager.unRegister(service);
    }

	public ExecutorServiceManager getExecutorServiceManager() {
		return executorServiceManager;
	}
}

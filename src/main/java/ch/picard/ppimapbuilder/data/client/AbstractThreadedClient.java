package ch.picard.ppimapbuilder.data.client;

import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;

/**
 * Abstract class providing base element for threaded web service clients.
 */
public abstract class AbstractThreadedClient {

    private final ExecutorServiceManager executorServiceManager;

    protected AbstractThreadedClient(ExecutorServiceManager executorServiceManager) {
        this.executorServiceManager = executorServiceManager;
    }

	public ExecutorServiceManager getExecutorServiceManager() {
		return executorServiceManager;
	}
}

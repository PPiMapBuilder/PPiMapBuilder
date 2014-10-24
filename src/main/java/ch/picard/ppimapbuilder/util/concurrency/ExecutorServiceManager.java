package ch.picard.ppimapbuilder.util.concurrency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {

    protected final Integer maxNumberThread;

    private final Set<ExecutorService> inUseExecutorServices;
    private final Set<ExecutorService> notInUseExecutorServices;

    public ExecutorServiceManager(Integer maxNumberThread) {
        this.inUseExecutorServices = new HashSet<ExecutorService>();
        this.notInUseExecutorServices = new HashSet<ExecutorService>();
        this.maxNumberThread = maxNumberThread;
    }

    public synchronized List<ExecutorService> getExecutorServices() {
        List<ExecutorService> services = new ArrayList<ExecutorService>();
        services.addAll(inUseExecutorServices);
        services.addAll(notInUseExecutorServices);
        return services;
    }

    public synchronized ExecutorService getOrCreateThreadPool() {
        return maxNumberThread != null ?
                getOrCreateThreadPool(maxNumberThread) :
                null;
    }

    public synchronized void unRegister(ExecutorService service) {
        inUseExecutorServices.remove(service);
        notInUseExecutorServices.add(service);
    }

    private ExecutorService getOrCreateThreadPool(int nbThread) {
        for (ExecutorService service : notInUseExecutorServices)
            return register(service);
        return register(Executors.newFixedThreadPool(nbThread));
    }

    private ExecutorService register(ExecutorService service) {
        notInUseExecutorServices.remove(service);
        inUseExecutorServices.add(service);
        return service;
    }

	public Integer getMaxNumberThread() {
		return maxNumberThread;
	}

	public void shutdown() {
		for (ExecutorService service : getExecutorServices()) {
			if (!service.isShutdown() || !service.isTerminated())
				service.shutdownNow();
		}
	}

}

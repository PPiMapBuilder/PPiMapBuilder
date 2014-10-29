package ch.picard.ppimapbuilder.util.concurrency;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceManager {

	public static final Integer DEFAULT_NB_THREAD = Runtime.getRuntime().availableProcessors()+1;
	protected final Integer maxNumberThread;

	private final Set<ExecutorService> allExecutorServices;
    private final Set<ExecutorService> inUseExecutorServices;
    private final Set<ExecutorService> freeExecutorServices;

	public ExecutorServiceManager() {
		this(DEFAULT_NB_THREAD);
	}

    public ExecutorServiceManager(Integer maxNumberThread) {
	    this.allExecutorServices = new HashSet<ExecutorService>();
        this.inUseExecutorServices = new HashSet<ExecutorService>();
        this.freeExecutorServices = new HashSet<ExecutorService>();
        this.maxNumberThread = maxNumberThread;
    }

    public synchronized ExecutorService getOrCreateThreadPool() {
        if(maxNumberThread == null) return null;
	    for (ExecutorService service : freeExecutorServices)
		    return register(service);
	    return createThreadPool(maxNumberThread);
    }

	public synchronized ExecutorService createThreadPool(int nbThread) {
		return register(Executors.newFixedThreadPool(nbThread));
	}

    public synchronized void unRegister(ExecutorService service) {
        inUseExecutorServices.remove(service);
        freeExecutorServices.add(service);
    }

    private ExecutorService register(ExecutorService service) {
	    allExecutorServices.add(service);
        freeExecutorServices.remove(service);
        inUseExecutorServices.add(service);
        return service;
    }

	public synchronized void remove(ExecutorService service) {
		allExecutorServices.remove(service);
		inUseExecutorServices.remove(service);
		freeExecutorServices.remove(service);
	}

	public Integer getMaxNumberThread() {
		return maxNumberThread;
	}

	public void shutdown() {
		for (ExecutorService service : allExecutorServices)
			if (!service.isShutdown() || !service.isTerminated())
				service.shutdownNow();
	}

	public void clear() {
		allExecutorServices.clear();
		inUseExecutorServices.clear();
		freeExecutorServices.clear();
	}
}

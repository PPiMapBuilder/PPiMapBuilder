/*
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 *
 */

package ch.picard.ppimapbuilder.util.concurrent;

import java.util.HashSet;
import java.util.Set;
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
	    if(freeExecutorServices.size() >= Math.pow(DEFAULT_NB_THREAD, 2)+1)
		    remove(service);
	    else {
		    inUseExecutorServices.remove(service);
		    freeExecutorServices.add(service);
	    }
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

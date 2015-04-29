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
    
package ch.picard.ppimapbuilder.data.client;

import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;

import java.util.*;
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

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

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.interaction.client.web.ThreadedPsicquicClient;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ProteinOrthologWebCachedClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.web.InParanoidClient;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Manages all threaded web service clients used in the querying of the network.
 */
public class ThreadedClientManager {

    private final ExecutorServiceManager executorServiceManager;

	private final PMBProteinOrthologCacheClient proteinOrthologCacheClient;
	private final InParanoidClient inParanoidClient;
	private final List<PsicquicService> selectedDatabases;

	private final Set<AbstractThreadedClient> notInUseClients;

	public ThreadedClientManager(ExecutorServiceManager executorServiceManager, List<PsicquicService> selectedDatabases) {
        this.executorServiceManager = executorServiceManager;
        this.selectedDatabases = selectedDatabases;

		// InParanoid Client
		inParanoidClient = new InParanoidClient();
		inParanoidClient.setCache(PMBProteinOrthologCacheClient.getInstance()); //XML response cache

		// PMB ortholog cache client
		proteinOrthologCacheClient = PMBProteinOrthologCacheClient.getInstance();

		this.notInUseClients = new HashSet<AbstractThreadedClient>();
	}

	private <T> T getUnUsedClientByClass(Class<T> clientClass) {
		for (AbstractThreadedClient client : notInUseClients) {
			if (clientClass.isInstance(client))
				return clientClass.cast(client);
		}
		return null;
	}

	private <T extends AbstractThreadedClient> T register(T client) {
		notInUseClients.remove(client);
		return client;
	}

    public synchronized ThreadedProteinOrthologClientDecorator getOrCreateProteinOrthologClient() {
		ThreadedProteinOrthologClientDecorator client = getUnUsedClientByClass(ThreadedProteinOrthologClientDecorator.class);
		if (client == null) {
			client = new ThreadedProteinOrthologClientDecorator(
					new ProteinOrthologWebCachedClient(inParanoidClient, proteinOrthologCacheClient),
					executorServiceManager
			);
		}
		return register(client);
	}

	public synchronized UniProtEntryClient getOrCreateUniProtClient() {
		UniProtEntryClient client = getUnUsedClientByClass(UniProtEntryClient.class);
		if (client == null) client = new UniProtEntryClient(executorServiceManager);
		return register(client);
	}

	public synchronized ThreadedPsicquicClient getOrCreatePsicquicClient() {
		ThreadedPsicquicClient client = getUnUsedClientByClass(ThreadedPsicquicClient.class);
		if (client == null) client = new ThreadedPsicquicClient(selectedDatabases, executorServiceManager);
		return register(client);
	}

	public ExecutorServiceManager getExecutorServiceManager() {
		return executorServiceManager;
	}

	public synchronized void unRegister(AbstractThreadedClient client) {
		notInUseClients.add(client);
	}

	public void clear() {
		proteinOrthologCacheClient.clearMemoryCache();
		notInUseClients.clear();
		executorServiceManager.clear();
	}

	public void shutdown() {
		executorServiceManager.shutdown();
	}
}
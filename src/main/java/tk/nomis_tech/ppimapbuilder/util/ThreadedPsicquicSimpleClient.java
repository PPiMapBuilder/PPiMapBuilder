package tk.nomis_tech.ppimapbuilder.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import uk.ac.ebi.enfin.mi.cluster.Encore2Binary;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

import com.google.common.collect.Lists;

/**
 * An advanced PSICQUIC simple client capable of querying multiple service with
 * multiple thread.<br/>
 * Also makes a cluster (MiCluster) of resulted interaction to remove
 * duplicates.
 */
public class ThreadedPsicquicSimpleClient {

	private final List<PsicquicService> services;
	private final int nbThread;

	/**
	 * @param serviceRestUrl
	 *            list of PSICQUIC service REST url
	 * @param nbThread
	 *            number of parallel request that need to be sent
	 */
	public ThreadedPsicquicSimpleClient(List<PsicquicService> services, int nbThread) {
		this.services = services;
		this.nbThread = nbThread;
	}
	
	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded request over
	 * multiple PSICQUIC service
	 */
	public List<BinaryInteraction> getByQuery(final String query) {
		final List<Future<List<BinaryInteraction>>> requests = new ArrayList<Future<List<BinaryInteraction>>>();
		final ExecutorService executor = Executors.newFixedThreadPool(nbThread);
		final CompletionService<List<BinaryInteraction>> completionService = new ExecutorCompletionService<List<BinaryInteraction>>(executor);
		
		// Launch and store interaction requests
		for (final PsicquicService service : services) {
			requests.add(completionService.submit(new Callable<List<BinaryInteraction>>() {
				@Override
				public List<BinaryInteraction> call() throws Exception {
					final PsicquicSimpleClient client = new PsicquicSimpleClient(service.getRestUrl());
					final InputStream result = client.getByQuery(query, PsicquicSimpleClient.MITAB25);
					
					final PsimiTabReader mitabReader = new PsimiTabReader();
					return Lists.newArrayList(mitabReader.read(result));
				}
			}));
		}
		
		// Collect all interaction results
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		for (int i = 0; i < requests.size(); i++) {
			Future<List<BinaryInteraction>> req = requests.get(i);

			try {
				results.addAll(completionService.take().get());
			} catch (ExecutionException e) {
				System.err.println(services.get(i).getName() + " interaction query failed");
				//e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

}

package tk.nomis_tech.ppimapbuilder.util;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

/**
 * An advanced PSICQUIC simple client capable of querying multiple service with multiple thread.<br/>
 * Also makes a cluster (MiCluster) of resulted interaction to remove duplicates.
 */
public class ThreadedPsicquicSimpleClient {

	private final List<PsicquicService> services;
	private final int NB_THREAD;

	/**
	 * Constructs a new ThreadedPsicquicSimpleClient
	 * @param services list of PSICQUIC services that will be use during query
	 * @param NB_THREAD number of parallel request that need to be sent
	 */
	public ThreadedPsicquicSimpleClient(List<PsicquicService> services, final int NB_THREAD) {
		this.services = services;
		this.NB_THREAD = NB_THREAD;
	}

	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded request over multiple PSICQUIC services
	 */
	public List<BinaryInteraction> getByQuery(final String query) throws Exception {
		final List<Future<List<BinaryInteraction>>> requests = new ArrayList<Future<List<BinaryInteraction>>>();
		final ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		final CompletionService<List<BinaryInteraction>> completionService = new ExecutorCompletionService<List<BinaryInteraction>>(
				executor);

		// Launch and store interaction requests
		for (final PsicquicService service : services) {
			requests.add(completionService.submit(new Callable<List<BinaryInteraction>>() {
				@Override
				public List<BinaryInteraction> call() throws Exception {
					// if(service.getName().contains("GeneMANIA")) return new ArrayList<BinaryInteraction>();
					List<BinaryInteraction> result = null;

					final int MAX_TRY = 2;
					int i = 0;
					while (result == null) {
						try {
							final PsicquicSimpleClient client = new PsicquicSimpleClient(service.getRestUrl());
							final InputStream mitabResult = client.getByQuery(query, PsicquicSimpleClient.MITAB25);

							final PsimiTabReader mitabReader = new PsimiTabReader();
							result = (List<BinaryInteraction>) mitabReader.read(mitabResult);
						} catch (Exception e) {
							if (++i >= MAX_TRY)
								throw e;
							// System.out.println("Retrying "+service.getName()+" interaction query");
						}
					}

					return result;
				}
			}));
		}

		// Collect all interaction results
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		for (int i = 0; i < requests.size(); i++) {
			Future<List<BinaryInteraction>> take = null;
			try {
				take = completionService.take();
				results.addAll(take.get());
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();

				if (cause != null) {
					if(cause instanceof SocketTimeoutException) {
						//Connection failed to a remote database (error from the database)
						System.err.println(services.get(requests.indexOf(take)).getName()+" server error");
					}
					else if(cause instanceof UnknownHostException) {
						//No internet connection to the database
						System.err.println(services.get(requests.indexOf(take)).getName()+" connection failed");
					}
				} else
					throw e;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.gc();

		return results;
	}
	
	/**
	 * Gets cumulative list of interaction from a list of MiQL query
	 */
	public List<BinaryInteraction> getByQueries(final List<String> queries) throws Exception {
		// Thread manager
		ExecutorService executor = Executors.newFixedThreadPool(NB_THREAD);
		CompletionService<List<BinaryInteraction>> completionService = new ExecutorCompletionService<List<BinaryInteraction>>(executor);

		// Launch queries in thread
		List<Future<List<BinaryInteraction>>> interactionRequests = new ArrayList<Future<List<BinaryInteraction>>>();
		for (final String query : queries) {
			interactionRequests.add(completionService.submit(new Callable<List<BinaryInteraction>>() {
				@Override
				public List<BinaryInteraction> call() throws Exception {
					List<BinaryInteraction> result = null;

					final int MAX_TRY = 2;
					int i = 0;
					while (result == null) {
						try {
							result = (List<BinaryInteraction>) getByQuery(query);
							// System.out.println((queries.indexOf(query)+1)+"/"+queries.size());
						} catch (Exception e) {
							if (++i >= MAX_TRY)
								throw e;
						}
					}

					return result;
				}
			}));
		}

		List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		// Collect all interaction results
		for (int i = 0; i < interactionRequests.size(); i++) {
			try {
				results.addAll(completionService.take().get());
			} catch (ExecutionException e) {
				// if(!(e.getCause() instanceof NullPointerException)) {
				System.err.println("Interaction query failed -> " + e.getMessage());
				throw e;
				// }
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return results;
	}
}

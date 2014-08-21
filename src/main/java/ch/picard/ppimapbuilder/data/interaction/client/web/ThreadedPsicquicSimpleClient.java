package ch.picard.ppimapbuilder.data.interaction.client.web;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * An advanced PSICQUIC simple client capable of querying multiple service with multiple thread.<br/>
 * Also makes a cluster (MiCluster) of resulted interaction to remove duplicates.
 */
public class ThreadedPsicquicSimpleClient extends AbstractThreadedClient {

	private final List<PsicquicService> services;

	/**
	 * Constructs a new ThreadedPsicquicSimpleClient
	 * @param services list of PSICQUIC services that will be use during query
	 */
	public ThreadedPsicquicSimpleClient(List<PsicquicService> services) {
		this(services, null);
	}

	/**
	 * Constructs a new ThreadedPsicquicSimpleClient
	 * @param services list of PSICQUIC services that will be use during query
	 */
	public ThreadedPsicquicSimpleClient(List<PsicquicService> services, Integer nbThread) {
		super(nbThread);
		this.services = services;
	}

	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded request over multiple PSICQUIC services
	 */
	public List<BinaryInteraction> getByQuery(final String query) throws Exception {
		final List<Future<List<BinaryInteraction>>> requests = new ArrayList<Future<List<BinaryInteraction>>>();

		ExecutorService executorService = newThreadPool();

		final CompletionService<List<BinaryInteraction>> completionService =
				new ExecutorCompletionService<List<BinaryInteraction>>(executorService);

		// Launch and organism interaction requests
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

			if (executorService.isShutdown())
				break;

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

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		// Thread manager
		final CompletionService<List<BinaryInteraction>> completionService = new ExecutorCompletionService<List<BinaryInteraction>>(executorService);

		// Launch queries in thread
		final List<Future<List<BinaryInteraction>>> interactionRequests = new ArrayList<Future<List<BinaryInteraction>>>();
		for (final String query : queries) {
			interactionRequests.add(completionService.submit(new Callable<List<BinaryInteraction>>() {
				@Override
				public List<BinaryInteraction> call() throws Exception {
					List<BinaryInteraction> result = null;

					final int MAX_TRY = 2;
					int i = 0;
					while (result == null) {
						try {
							result = getByQuery(query);
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

		// Collect all interaction results
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		for (Future<List<BinaryInteraction>> ignored : interactionRequests) {

			if (executorService.isShutdown())
				break;

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

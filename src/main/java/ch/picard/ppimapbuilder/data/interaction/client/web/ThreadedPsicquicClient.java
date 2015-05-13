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
    
package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.SteppedProgressMonitorFactory;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import com.google.common.collect.Lists;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * /!\ ThreadedPsicquicClient will be replaced by PsicquicRequestBuilder
 *
 * A PSICQUIC client capable of querying multiple service with multiple thread.<br/>
 * Also makes a cluster (MiCluster) of resulted interaction to remove duplicates.
 */
//@Deprecated
public class ThreadedPsicquicClient extends AbstractThreadedClient {

	// Clients for each services
	private final List<PsicquicService> services;
	private final Map<PsicquicService, PsicquicSimpleClient> clients;

	// PSIMI-Tab reader
	private final PsimiTabReader mitabReader = new PsimiTabReader();

	/**
	 * Constructs a new ThreadedPsicquicSimpleClient
	 *
	 * @param services list of PSICQUIC services that will be use during query
	 */
	public ThreadedPsicquicClient(Collection<PsicquicService> services, ExecutorServiceManager executorServiceManager) {
		super(executorServiceManager);

		this.services = new ArrayList<PsicquicService>(services);
		this.clients = new HashMap<PsicquicService, PsicquicSimpleClient>(services.size());
		for (PsicquicService service : services) {
			clients.put(service, new PsicquicSimpleClient(service.getRestUrl()));
		}
	}

	/**
	 * Fetches interactions from query on a PsicquicService. If the request result size is over a thousand,
	 * the request is spliced and the requests are executed concurrently.
	 */
	protected List<BinaryInteraction> getByQuerySimple(final PsicquicService service, final String query, final ProgressMonitor progressMonitor) throws IOException, PsimiTabException {
		final int MAX_TRY = 2;
		int i = 0;
		final Throwable[] error = new Throwable[1];
		while (++i <= MAX_TRY) {
			final PsicquicSimpleClient psicquicSimpleClient = clients.get(service);

			long count = psicquicSimpleClient.countByQuery(query);
			final int maxResults = 1000;
			final int numberPages = (int) Math.ceil((double) count / (double) maxResults);

			final List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>();
			final double[] nbDone = new double[]{0d};
			new ConcurrentExecutor<Collection<BinaryInteraction>>(getExecutorServiceManager(), numberPages) {
				@Override
				public Callable<Collection<BinaryInteraction>> submitRequests(final int index) {
					final int firstResult = index * maxResults;
					return new Callable<Collection<BinaryInteraction>>() {
						@Override
						public Collection<BinaryInteraction> call() throws Exception {
							return mitabReader.read(
									psicquicSimpleClient
											.getByQuery(
													query,
													PsicquicSimpleClient.MITAB25,
													firstResult,
													maxResults
											)
							);
						}
					};
				}

				@Override
				public void processResult(Collection<BinaryInteraction> intermediaryResult, Integer index) {
					if (progressMonitor != null) {
						progressMonitor.setProgress(++nbDone[0] / numberPages);
					}
					interactions.addAll(intermediaryResult);
				}

				@Override
				public boolean processExecutionException(ExecutionException e, Integer index) {
					error[0] = e.getCause();
					return true;
				}
			}.run();

			if (error[0] == null)
				return interactions;
		}
		if (error[0] != null) {
			if (error[0] instanceof IOException) throw (IOException) error[0];
			else if (error[0] instanceof PsimiTabException) throw (PsimiTabException) error[0];
		}
		return null;
	}

	protected List<BinaryInteraction> getProteinInteractorSimple(PsicquicService service, Protein protein) throws IOException, PsimiTabException {
		return (List<BinaryInteraction>) mitabReader.read(
				clients
						.get(service)
						.getByInteractor(
								protein.getUniProtId(),
								PsicquicSimpleClient.MITAB25
						)
		);
	}

	/**
	 * Same as PsicquicSimpleClient.getByInteractor but with threaded requests over multiple PSICQUIC services
	 */
	public List<BinaryInteraction> getByInteractor(final Protein protein) throws Exception {
		final ArrayList<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		new ConcurrentExecutor<List<BinaryInteraction>>(getExecutorServiceManager(), services.size()) {
			@Override
			public Callable<List<BinaryInteraction>> submitRequests(final int index) {
				return new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						return getProteinInteractorSimple(services.get(index), protein);
					}
				};
			}

			@Override
			public void processResult(List<BinaryInteraction> intermediaryResult, Integer index) {
				results.addAll(intermediaryResult);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				Throwable cause = e.getCause();

				if (cause != null) {
					if (cause instanceof SocketTimeoutException) {
						//Connection failed to a remote database (error from the database)
						if (index != null)
							System.err.println(services.get(index).getName() + " server error");
					} else if (cause instanceof UnknownHostException) {
						//No internet connection to the database (no internet or server no longer exists)
						if (index != null)
							System.err.println(services.get(index).getName() + " connection failed");
					}
				} else {
					//unknown error
				}
				return false;
			}
		}.run();
		return results;
	}

	/**
	 * Fetches cumulative list of interactions for a given list on interactors.
	 */
	public List<BinaryInteraction> getByInteractors(final Collection<Protein> proteins) {
		final List<Protein> proteinList = new ArrayList<Protein>(proteins);
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();

		new ConcurrentExecutor<List<BinaryInteraction>>(getExecutorServiceManager(), proteinList.size()) {
			@Override
			public Callable<List<BinaryInteraction>> submitRequests(final int index) {
				return new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						return getByInteractor(proteinList.get(index));
					}
				};
			}

			@Override
			public void processResult(List<BinaryInteraction> intermediaryResult, Integer index) {
				results.addAll(intermediaryResult);
			}

		}.run();

		return results;
	}

	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded requests over multiple PSICQUIC services
	 */
	public List<BinaryInteraction> getByQuery(final String query) throws Exception {
		return getByQuery(query, null);
	}

	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded requests over multiple PSICQUIC services and with a
	 * ProgressMonitor
	 */
	public List<BinaryInteraction> getByQuery(final String query, final ProgressMonitor progressMonitor) throws Exception {
		final SteppedProgressMonitorFactory monitorFactory =
				progressMonitor != null ?
						new SteppedProgressMonitorFactory(progressMonitor, services.size()):
						null;

		final ArrayList<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		new ConcurrentExecutor<List<BinaryInteraction>>(getExecutorServiceManager(), services.size()) {
			@Override
			public Callable<List<BinaryInteraction>> submitRequests(final int index) {
				return new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						return getByQuerySimple(
								services.get(index),
								query,
								monitorFactory != null ?
										monitorFactory.createStepProgressMonitor(index) :
										null
						);
					}
				};
			}

			@Override
			public void processResult(List<BinaryInteraction> intermediaryResult, Integer index) {
				results.addAll(intermediaryResult);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				Throwable cause = e.getCause();

				if (cause != null) {
					if (cause instanceof SocketTimeoutException) {
						//Connection failed to a remote database (error from the database)
						if (index != null)
							System.err.println(services.get(index).getName() + " server error");
					} else if (cause instanceof UnknownHostException) {
						//No internet connection to the database (no internet or server no longer exists)
						if (index != null)
							System.err.println(services.get(index).getName() + " connection failed");
					}
				} else {
					//unknown error
				}
				return false;
			}
		}.run();

		return results;
	}

	/**
	 * Gets cumulative list of interaction from a list of MiQL query
	 */
	public List<BinaryInteraction> getByQueries(final Collection<String> queries) throws Exception {
		return getByQueries(queries, null);
	}

	/**
	 * Gets cumulative list of interaction from a list of MiQL query with a progress indicator
	 */
	public List<BinaryInteraction> getByQueries(final Collection<String> queries, final ProgressMonitor progressMonitor) throws Exception {
		final List<String> queryList = new ArrayList<String>(queries);
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();

		new ConcurrentExecutor<List<BinaryInteraction>>(getExecutorServiceManager(), queryList.size()) {
			@Override
			public Callable<List<BinaryInteraction>> submitRequests(final int index) {
				return new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						return getByQuery(queryList.get(index));
					}
				};
			}

			@Override
			public void processResult(List<BinaryInteraction> intermediaryResult, Integer index) {
				if (progressMonitor != null) progressMonitor.setProgress(index / queryList.size());
				results.addAll(intermediaryResult);
			}

		}.run();

		return results;
	}

	/**
	 * Retrieve all interactions with the given interactors (optimized and threaded)
	 */
	public List<BinaryInteraction> getInteractionsInProteinPool(Set<String> proteins, Organism sourceOrganism) throws Exception {

		if (proteins.size() <= 1)
			return new ArrayList<BinaryInteraction>();

		List<String> sourceProteins = Lists.newArrayList(proteins);
		MiQLExpressionBuilder baseQuery = new MiQLExpressionBuilder();
		baseQuery.setRoot(true);
		baseQuery.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("species", sourceOrganism.getTaxId()));

		// baseInteractionQuery.addParam(new MiQLParameterBuilder("type", "association"));

		// Create idA and idB parameters
		MiQLParameterBuilder idA, idB;
		MiQLExpressionBuilder prots = new MiQLExpressionBuilder();
		{
			prots.addAll(sourceProteins);
			idA = new MiQLParameterBuilder("idA", prots);
			idB = new MiQLParameterBuilder("idB", prots);
		}

		// Calculate the estimated url query length
		final int BASE_URL_LENGTH = 100;
		int estimatedURLQueryLength = 0;
		int idParamLength = 0, baseParamLength = 0;
		{
			try {
				idParamLength = URLEncoder.encode(idB.toString(), "UTF-8").length() + URLEncoder.encode(idA.toString(), "UTF-8").length();
				baseParamLength = URLEncoder.encode(baseQuery.toString(), "UTF-8").length();

				estimatedURLQueryLength = BASE_URL_LENGTH + baseParamLength + idParamLength;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// Slice the query in multiple queries if the result MiQL query is
		// bigger than maxQuerySize
		final List<String> queries = new ArrayList<String>();
		final int MAX_QUERY_SIZE = BASE_URL_LENGTH + baseParamLength + 950;//TODO check difference in result when changing url length
		{
			if (estimatedURLQueryLength > MAX_QUERY_SIZE) {

				final int STEP_LENGTH = (int) Math.ceil((double) (MAX_QUERY_SIZE - BASE_URL_LENGTH - baseParamLength) * sourceProteins.size()
						/ (double) idParamLength);
				final int NB_TRUNCATION = (int) Math.ceil((double) sourceProteins.size() / (double) STEP_LENGTH);

				//System.out.println("N# proteins: " + sourceProteins.size());
				//System.out.println("N# queries: " + NB_TRUNCATION);

				// Generate truncated protein listing
				// Ex: "prot1", "prot2", "prot3", "prot4" => ("prot1", "prot2"), ("prot3", "prot4")
				final List<MiQLExpressionBuilder> protsExprs = new ArrayList<MiQLExpressionBuilder>();
				int pos = 0;
				for (int i = 0; i < NB_TRUNCATION; i++) {
					int from = pos;
					int to = Math.min(from + STEP_LENGTH, sourceProteins.size());

					MiQLExpressionBuilder protsTruncated = new MiQLExpressionBuilder();
					protsTruncated.addAll(sourceProteins.subList(from, to));
					protsExprs.add(protsTruncated);

					pos = to;
				}
				MiQLExpressionBuilder protsIdA, protsIdB;
				for (int i = 0; i < protsExprs.size(); i++) {
					protsIdA = protsExprs.get(i);
					//System.out.println(protsIdA);

					for (int j = i; j < protsExprs.size(); j++) {
						protsIdB = protsExprs.get(j);
						MiQLExpressionBuilder q = new MiQLExpressionBuilder(baseQuery);
						q.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("idA", protsIdA));
						q.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("idB", protsIdB));
						queries.add(q.toString());
						//System.out.println(q);
					}
				}
			} else {
				baseQuery.add(MiQLExpressionBuilder.Operator.AND, idA);
				baseQuery.add(MiQLExpressionBuilder.Operator.AND, idB);
				queries.add(baseQuery.toString());
			}
			System.gc();
		}

		//System.out.println(queries.size());

		// Executing all MiQL queries using ThreadedPsicquicSimpleClient
		return getByQueries(queries);
	}
}

package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.ConcurrentExecutor;
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
import java.util.concurrent.*;

/**
 * A PSICQUIC client capable of querying multiple service with multiple thread.<br/>
 * Also makes a cluster (MiCluster) of resulted interaction to remove duplicates.
 */
public class ThreadedPsicquicClient extends AbstractThreadedClient {

	// Clients for each services
	private final Map<PsicquicService, PsicquicSimpleClient> clients;

	// PSIMI-Tab reader
	private final PsimiTabReader mitabReader = new PsimiTabReader();

	// Executor services
	private final ExecutorService lvl1ExecutorService;
	private final Map<ExecutorService, Boolean> lvl2ExecutorServices;

	/**
	 * Constructs a new ThreadedPsicquicSimpleClient
	 * @param services list of PSICQUIC services that will be use during query
	 */
	public ThreadedPsicquicClient(List<PsicquicService> services, Integer maxNumberThread) {
		super(maxNumberThread);

		clients = new HashMap<PsicquicService, PsicquicSimpleClient>(services.size());
		for(PsicquicService service : services) {
			clients.put(service, new PsicquicSimpleClient(service.getRestUrl()));
		}

		this.lvl1ExecutorService = newThreadPool();
		this.lvl2ExecutorServices = new HashMap<ExecutorService, Boolean>();
		for(int i = 0; i < maxNumberThread; i++) {
			this.lvl2ExecutorServices.put(newThreadPool(maxNumberThread * 2), false);
		}
	}

	private ExecutorService getNotRunningLvl2ExecutorService() {
		for (ExecutorService lvl2ExecutorService : lvl2ExecutorServices.keySet()) {
			if(!lvl2ExecutorServices.get(lvl2ExecutorService)) {
				lvl2ExecutorServices.put(lvl2ExecutorService, true);
				return lvl2ExecutorService;
			}
		}
		return newThreadPool();
	}

	protected List<BinaryInteraction> getByQuerySimple(PsicquicService service, String query) throws IOException, PsimiTabException {
		return (List<BinaryInteraction>) mitabReader.read(
				clients
						.get(service)
						.getByQuery(
								query,
								PsicquicSimpleClient.MITAB25
						)
		);
	}

	/**
	 * Same as PsicquicSimpleClient.getByQuery but with threaded request over multiple PSICQUIC services
	 */
	public List<BinaryInteraction> getByQuery(final String query) throws Exception {
		final List<PsicquicService> services = new ArrayList<PsicquicService>(clients.keySet());

		final ArrayList<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		final ExecutorService executorService = getNotRunningLvl2ExecutorService();
		new ConcurrentExecutor<List<BinaryInteraction>>(executorService, services.size()) {
			@Override
			public Callable<List<BinaryInteraction>> submitRequests(final int index) {
				return new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						List<BinaryInteraction> result = null;

						final int MAX_TRY = 2;
						int i = 0;
						while (result == null) {
							Exception error = null;
							try {
								result = getByQuerySimple(services.get(index), query);
							} catch (Exception e) {
								error = e;
								// System.out.println("Retrying "+service.getName()+" interaction query");
							}

							if (++i >= MAX_TRY) {
								if(error != null)
									throw error;
							} else break;
						}

						return result;
					}
				};
			}

			@Override
			public void processResult(List<BinaryInteraction> result, Integer index) {
				results.addAll(result);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				Throwable cause = e.getCause();

				if (cause != null) {
					if(cause instanceof SocketTimeoutException) {
						//Connection failed to a remote database (error from the database)
						if(index != null)
							System.err.println(services.get(index).getName()+" server error");
					}
					else if(cause instanceof UnknownHostException) {
						//No internet connection to the database (no internet or server no longer exists)
						if(index != null)
							System.err.println(services.get(index).getName()+" connection failed");
					}
				} else {
					//unknown error
				}
				return false;
			}
		}.run();

		lvl2ExecutorServices.put(executorService, false);
		return results;
	}
	
	/**
	 * Gets cumulative list of interaction from a list of MiQL query
	 */
	public synchronized List<BinaryInteraction> getByQueries(final Collection<String> queries) throws Exception {

		final List<String> queryList = new ArrayList<String>(queries);
		final List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();

		new ConcurrentExecutor<List<BinaryInteraction>>(lvl1ExecutorService, queryList.size()) {
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
			public void processResult(List<BinaryInteraction> result, Integer index) {
				results.addAll(result);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				System.err.println("Interaction query failed -> " + e.getMessage());
				return false;
			}

		}.run();

		return results;
	}

	/**
	 * Retrieve all interactions with the given interactors (optimized and threaded)
	 */
	public List<BinaryInteraction> getInteractionsInProteinPool(Set<Protein> proteins, Organism sourceOrganism) throws Exception {

		if (proteins.size() <= 1)
			return new ArrayList<BinaryInteraction>();

		List<Protein> sourceProteins = Lists.newArrayList(proteins);
		MiQLExpressionBuilder baseQuery = new MiQLExpressionBuilder();
		baseQuery.setRoot(true);
		baseQuery.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("species", sourceOrganism.getTaxId()));

		// baseInteractionQuery.addParam(new MiQLParameterBuilder("type", "association"));

		// Create idA and idB parameters
		MiQLParameterBuilder idA, idB;
		MiQLExpressionBuilder prots = new MiQLExpressionBuilder();
		{
			for (Protein protein : proteins)
				prots.add(protein.getUniProtId());
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
					for (Protein protein : sourceProteins.subList(from, to))
						protsTruncated.add(protein.getUniProtId());
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
						q.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("idA", protsIdA));
						q.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("idB", protsIdB));
						queries.add(q.toString());
						//System.out.println(q);
					}
				}
			} else {
				baseQuery.addCondition(MiQLExpressionBuilder.Operator.AND, idA);
				baseQuery.addCondition(MiQLExpressionBuilder.Operator.AND, idB);
				queries.add(baseQuery.toString());
			}
			System.gc();
		}

		//System.out.println(queries.size());

		// Executing all MiQL queries using ThreadedPsicquicSimpleClient
		return getByQueries(queries);
	}
}

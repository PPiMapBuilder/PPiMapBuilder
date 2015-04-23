package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import com.google.common.collect.Lists;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PsicquicRequestBuilder {

	private final List<PsicquicSimpleClient> clients;
	private final List<PsicquicRequest> requests;
	private int maxResultsPerPages = 1000;
	private int estimatedInteractionsCount = 0;

	public PsicquicRequestBuilder(Collection<PsicquicService> services) {
		this(InteractionUtils.psicquicServicesToPsicquicSimpleClients(services));
	}

	public PsicquicRequestBuilder(List<PsicquicSimpleClient> clients) {
		this.clients = new ArrayList<PsicquicSimpleClient>(clients);
		requests = new ArrayList<PsicquicRequest>();
	}

	public PsicquicRequestBuilder setMaxResultsPerPages(int maxResultsPerPages) {
		this.maxResultsPerPages = maxResultsPerPages;
		return this;
	}

	public PsicquicRequestBuilder addQuery(final MiQLExpressionBuilder query) {
		return addQuery(query.toString());
	}

	/**
	 * Create PSICQUIC request for a query on multiple databases and pagination of max 1000 interactions per page
	 */
	public PsicquicRequestBuilder addQuery(final String query) {
		for (PsicquicSimpleClient client : clients) {
			try {
				long count = client.countByQuery(query);
				estimatedInteractionsCount += count;
				final int numberPages = (int) Math.ceil((double) count / (double) maxResultsPerPages);

				for (int page = 0; page < numberPages; page++) {
					final int firstResult = page * maxResultsPerPages;
					requests.add(new PsicquicRequest(client, query, firstResult, maxResultsPerPages));
				}
			} catch (IOException e) {
				requests.add(new PsicquicRequest(client, query, 0, Integer.MAX_VALUE));
			}
		}
		return this;
	}

	public PsicquicRequestBuilder addQueries(List<String> queries) {
		for (String query : queries) {
			addQuery(query);
		}
		return this;
	}

	public PsicquicRequestBuilder addGetByTaxon(final Integer taxonId) {
		MiQLExpressionBuilder query = new MiQLExpressionBuilder();

		query.setRoot(true);
		query.add(new MiQLParameterBuilder("taxidA", taxonId));
		query.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("taxidB", taxonId));

		return addQuery(query);
	}

	public PsicquicRequestBuilder addGetByTaxonAndId(final String id, final Integer taxId) {
		MiQLExpressionBuilder query = new MiQLExpressionBuilder();

		query.setRoot(true);
		query.add(new MiQLParameterBuilder("taxidA", taxId));
		query.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("taxidB", taxId));
		query.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("id", id));

		return addQuery(query);
	}

	/**
	 * Create PSICQUIC requests for interactions between proteins in a specific organism
	 */
	public PsicquicRequestBuilder addGetByProteinPool(Set<String> proteins, Integer taxonId) {
		if (proteins.size() <= 1)
			return this;

		List<String> sourceProteins = Lists.newArrayList(proteins);
		MiQLExpressionBuilder baseQuery = new MiQLExpressionBuilder();
		baseQuery.setRoot(true);
		baseQuery.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("species", taxonId));

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
		final int MAX_QUERY_SIZE = BASE_URL_LENGTH + baseParamLength + 950;
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

						addQuery(q);
					}
				}
			} else {
				baseQuery.add(MiQLExpressionBuilder.Operator.AND, idA);
				baseQuery.add(MiQLExpressionBuilder.Operator.AND, idB);

				addQuery(baseQuery);
			}
		}
		return this;
	}

	public List<PsicquicRequest> getPsicquicRequests() {
		return requests;
	}

	public int getEstimatedInteractionsCount() {
		return estimatedInteractionsCount;
	}
}
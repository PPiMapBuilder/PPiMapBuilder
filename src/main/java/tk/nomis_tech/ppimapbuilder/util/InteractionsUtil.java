package tk.nomis_tech.ppimapbuilder.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.google.common.collect.Lists;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLParameterBuilder;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLExpressionBuilder.Operator;
import uk.ac.ebi.enfin.mi.cluster.Encore2Binary;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

/**
 * Group of method useful for manipulation of interaction list
 */
public class InteractionsUtil {

	/**
	 * Retrieve all interactions with the given interactors (optimized and threaded)
	 */
	public static List<BinaryInteraction> getInteractionBetweenProtein(HashSet<String> proteins, Integer sourceOrganism,
			final List<PsicquicService> services) throws Exception {
		List<String> sourceProteins = Lists.newArrayList(proteins);
		MiQLExpressionBuilder baseQuery = new MiQLExpressionBuilder();
		baseQuery.setRoot(true);
		baseQuery.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("species", sourceOrganism.toString()));

		// baseInteractionQuery.addParam(new MiQLParameterBuilder("type",
		// "association"));

		// Create idA and idB parameters
		MiQLParameterBuilder idA, idB;
		MiQLExpressionBuilder prots = new MiQLExpressionBuilder();
		{
			prots.addAll(sourceProteins);
			idA = new MiQLParameterBuilder("idA", prots);
			idB = new MiQLParameterBuilder("idB", prots);
		}

		// Calculate the estimated url query length
		final int baseURLLength = 100;
		int estimatedURLQueryLength = 0;
		int idLength = 0, baseQueryLength = 0;
		{
			try {
				idLength = URLEncoder.encode(idB.toString(), "UTF-8").length();
				baseQueryLength = URLEncoder.encode(baseQuery.toString()+idA.toString(), "UTF-8").length();

				estimatedURLQueryLength = baseURLLength + baseQueryLength + idLength;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// Slice the query in multiple queries if the result MiQL query is
		// bigger than maxQuerySize
		List<MiQLExpressionBuilder> queries = new ArrayList<MiQLExpressionBuilder>();
		final int maxQuerySize = baseURLLength + baseQueryLength + 50;
		{
			if (estimatedURLQueryLength > maxQuerySize) {
				//int stepLength = (int) Math.ceil((double) sourceProteins.size() / (double) nbQuery);
				int stepLength = (int) Math.ceil((double)(prots.size() * (maxQuerySize - baseQueryLength - baseURLLength)) / (double) idLength);
				int nbQuery = (int) Math.ceil((double) prots.size() / (double) stepLength);
				
				int pos = 0;
				for (int i = 0; i < nbQuery; i++) {
					int from = pos;
					int to = Math.min(from + stepLength, sourceProteins.size());
					
					MiQLExpressionBuilder protsTruncated = new MiQLExpressionBuilder();
					protsTruncated.addAll(sourceProteins.subList(from, to));
					idB = new MiQLParameterBuilder("idB", protsTruncated);
					
					MiQLExpressionBuilder q = new MiQLExpressionBuilder(baseQuery);
					q.addCondition(Operator.AND, idA);
					q.addCondition(Operator.AND, idB);
					queries.add(q);
					
					pos = to;
				}
			} else {
				baseQuery.addCondition(Operator.AND, idA);
				baseQuery.addCondition(Operator.AND, idB);
				queries.add(baseQuery);
			}
		}
		
		// Executing all MiQL query in threads
		List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		{
			final int nbThread = 5;
			List<Future<List<BinaryInteraction>>> interactionRequests = new ArrayList<Future<List<BinaryInteraction>>>();
			ExecutorService executor = Executors.newFixedThreadPool(nbThread);
			CompletionService<List<BinaryInteraction>> completionService = new ExecutorCompletionService<List<BinaryInteraction>>(executor);

			// Launch queries in thread
			for (final MiQLExpressionBuilder query : queries) {
				interactionRequests.add(completionService.submit(new Callable<List<BinaryInteraction>>() {
					@Override
					public List<BinaryInteraction> call() throws Exception {
						ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(services, nbThread);
						return client.getByQuery(query.toString());
					}
				}));
			}

			// Collect all interaction results
			for (int i = 0; i < interactionRequests.size(); i++) {
				Future<List<BinaryInteraction>> req = interactionRequests.get(i);

				try {
					results.addAll(completionService.take().get());
				} catch (ExecutionException e) {
					System.err.println("Interaction query #" + i + " failed- " + e.getMessage());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return results;
	}
	
	/**
	 * Converts a Collection&lt;EncoreInteraction&gt; into a
	 * List&lt;BinaryInteraction&lt;Interactor&gt;&gt;
	 */
	public static List<BinaryInteraction> convertEncoreInteraction(Collection<EncoreInteraction> interactions) {
		List<BinaryInteraction> convertedInteractions = new ArrayList<BinaryInteraction>(interactions.size());
		Iterator<EncoreInteraction> it = interactions.iterator();
		Encore2Binary converter = new Encore2Binary();
		while (it.hasNext()) {
			EncoreInteraction encoreInteraction = (EncoreInteraction) it.next();
			convertedInteractions.add(converter.getBinaryInteraction(encoreInteraction));
		}
		return convertedInteractions;
	}

	/**
	 * Uses to cluster interaction using MiCluster
	 */
	public static Collection<EncoreInteraction> clusterInteraction(List<BinaryInteraction> interactions) {
		// Cluster interaction results to remove duplicates
		InteractionCluster cluster = new InteractionCluster();
		cluster.setBinaryInteractionIterator(interactions.iterator());
		cluster.setMappingIdDbNames("uniprotkb");
		cluster.runService();

		return cluster.getInteractionMapping().values();
	}

	/**
	 * Retrieve only interactors from list of interactions
	 */
	public static List<String> getInteractorsEncore(Collection<EncoreInteraction> interactions) {
		HashSet<String> interactors = new HashSet<String>();

		Iterator<EncoreInteraction> iterator = interactions.iterator();
		while (iterator.hasNext()) {
			EncoreInteraction interaction = iterator.next();

			if (interaction.getInteractorAccsA().containsKey("uniprotkb")) {
				String id = interaction.getInteractorAccsA().get("uniprotkb");
				if (UniprotId.isValid(id))
					interactors.add(id);
			}
			if (interaction.getInteractorAccsB().containsKey("uniprotkb")) {
				String id = interaction.getInteractorAccsB().get("uniprotkb");
				if (UniprotId.isValid(id))
					interactors.add(id);
			}
		}

		return new ArrayList<String>(interactors);
	}

	/**
	 * Retrieve only interactors from list of interactions
	 */
	public static List<String> getInteractorsBinary(Collection<BinaryInteraction> interactions) {
		HashSet<String> interactors = new HashSet<String>();

		Iterator<BinaryInteraction> iterator = filterNonUniprot(interactions).iterator();
		while (iterator.hasNext()) {
			BinaryInteraction interaction = iterator.next();

			interactors.add(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier());
			interactors.add(interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
		}

		return new ArrayList<String>(interactors);
	}

	/**
	 * Remove all interaction from a list if at least one of the interactor
	 * doesn't have an "uniprotkb" identifier. Also sort the identifiers of
	 * interactor to make uniprotkb appear first
	 */
	public static Collection<BinaryInteraction> filterNonUniprot(Collection<BinaryInteraction> interactions) {
		List<BinaryInteraction> out = new ArrayList<BinaryInteraction>();

		Iterator<BinaryInteraction> it = interactions.iterator();
		while (it.hasNext()) {
			BinaryInteraction binaryInteraction = it.next();

			boolean ok = true;
			for (Interactor interactor : Arrays.asList(new Interactor[] { binaryInteraction.getInteractorA(),
					binaryInteraction.getInteractorB() })) {

				List<CrossReference> ids = interactor.getIdentifiers();
				//ids.addAll(interactor.getAlternativeIdentifiers());

				if (ids.size() == 1 && !ids.get(0).getDatabase().equals("uniprotkb")) {
					ok = false;
					break;
				}

				CrossReference uniprot = null;
				boolean hasUniprot = false;
				for (CrossReference ref : ids) {
					final boolean isUniprot = ref.getDatabase().equals("uniprotkb");
					if (!hasUniprot)
						uniprot = ref;
					hasUniprot = hasUniprot || isUniprot;
				}

				if (!hasUniprot) {
					ok = false;
					break;
				}

				List<CrossReference> sortedIdentifiers = new ArrayList<CrossReference>();
				ids.remove(uniprot);
				sortedIdentifiers.add(uniprot);
				sortedIdentifiers.addAll(ids);
				interactor.setIdentifiers(sortedIdentifiers);
			}

			if (ok)
				out.add(binaryInteraction);
		}

		return out;
	}
}

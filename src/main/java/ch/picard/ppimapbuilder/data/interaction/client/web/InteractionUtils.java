package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import com.google.common.collect.Lists;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Group of method useful for manipulation of interaction list
 */
public class InteractionUtils {

	/**
	 * Retrieve all interactions with the given interactors (optimized and threaded)
	 */
	public static List<BinaryInteraction> getInteractionsInProteinPool(Set<Protein> proteins, Organism sourceOrganism,
	                                                                   ThreadedPsicquicSimpleClient psicquicClient) throws Exception {
		if (proteins.size() <= 1)
			return new ArrayList<BinaryInteraction>();

		List<Protein> sourceProteins = Lists.newArrayList(proteins);
		MiQLExpressionBuilder baseQuery = new MiQLExpressionBuilder();
		baseQuery.setRoot(true);
		baseQuery.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("species", sourceOrganism.getTaxId()));

		// baseInteractionQuery.addParam(new MiQLParameterBuilder("type",
		// "association"));

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
		List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		results.addAll(psicquicClient.getByQueries(queries));

		return results;
	}

	/**
	 * Uses to cluster interaction using MiCluster
	 */
	public static Collection<EncoreInteraction> clusterInteraction(List<BinaryInteraction> interactions) {
		// Cluster interaction results to remove duplicates
		InteractionCluster cluster = new InteractionCluster(interactions);
		cluster.setMappingIdDbNames("uniprotkb");
		cluster.runService();

		return cluster.getInteractionMapping().values();
	}

	/**
	 * Retrieve only interactors from list of interactions
	 */
	public static Set<String> getInteractorsBinary(List<BinaryInteraction> interactions) {
		HashSet<String> interactors = new HashSet<String>();
		List<BinaryInteraction> copyInteractions = new ArrayList<BinaryInteraction>(interactions);


		// Interaction filtering
		InteractionUtils.filter(
				copyInteractions,
				new InteractionUtils.UniProtInteractionFilter()
		);
		for (BinaryInteraction interaction : interactions) {
			interactors.add(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier());
			interactors.add(interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
		}

		return interactors;
	}

	public static abstract class InteractionFilter {
		public boolean isValidInteraction(BinaryInteraction interaction) {
			return true;
		}

		public boolean isValidInteractor(Interactor interactor) {
			return true;
		}
	}

	public static final class OrganismInteractionFilter extends InteractionFilter {
		private final Organism organism;

		public OrganismInteractionFilter(Organism organism) {
			this.organism = organism;
		}

		@Override
		public boolean isValidInteractor(Interactor interactor) {
			return interactor.getOrganism().getTaxid().equals(String.valueOf(organism.getTaxId()));
		}

	}

	public static final class UniProtInteractionFilter extends InteractionFilter {
		@Override
		public boolean isValidInteractor(Interactor interactor) {
			final List<CrossReference> ids = interactor.getIdentifiers();
			ids.addAll(interactor.getAlternativeIdentifiers());

			if (ids.size() == 1 && !ids.get(0).getDatabase().equals("uniprotkb"))
				return false;

			CrossReference uniprot = null;
			boolean hasUniprot = false;
			for (CrossReference ref : ids) {
				final boolean isUniprot = ref.getDatabase().equals("uniprotkb");
				final boolean idValid = ProteinUtils.UniProtId.isValid(ref.getIdentifier());
				hasUniprot = hasUniprot || (isUniprot && idValid);
				if (hasUniprot) {
					uniprot = ref;
					break;
				}
			}

			if (!hasUniprot)
				return false;

			List<CrossReference> sortedIdentifiers = new ArrayList<CrossReference>();
			ids.remove(uniprot);
			sortedIdentifiers.add(uniprot);
			sortedIdentifiers.addAll(ids);
			interactor.setIdentifiers(sortedIdentifiers);

			return true;
		}
	}

	/**
	 * Filter a List of BinaryInteraction to keep only the interaction satisfying the filters InteractionFilter
	 * @param interactions
	 * @param filters
	 */
	public static void filter(List<BinaryInteraction> interactions, InteractionFilter... filters) {
		ArrayList<BinaryInteraction> invalidInteractions = new ArrayList<BinaryInteraction>();
		interactionLoop : for (BinaryInteraction interaction : interactions) {

			for (InteractionFilter filter : filters) {
				if(!filter.isValidInteraction(interaction)) {
					invalidInteractions.add(interaction);
					continue interactionLoop;
				}
			}

			for (Interactor interactor : Arrays.asList(new Interactor[]{interaction.getInteractorA(),
					interaction.getInteractorB()})) {

				for (InteractionFilter filter : filters) {
					if(!filter.isValidInteractor(interactor)) {
						invalidInteractions.add(interaction);
						continue interactionLoop;
					}
				}
			}
		}
		interactions.removeAll(invalidInteractions);
	}

	public static String generateMiQLQueryIDTaxID(final String id, final Integer taxId) {
		MiQLExpressionBuilder query = new MiQLExpressionBuilder();

		query.setRoot(true);
		query.add(new MiQLParameterBuilder("taxidA", taxId));
		query.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("taxidB", taxId));
		query.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("id", id));

		return query.toString();
	}

	/*
	//Converts a Collection&lt;EncoreInteraction&gt; into a Collection&lt;BinaryInteraction&lt;Interactor&gt;&gt;
	public static Collection<BinaryInteraction> convertEncoreInteraction(Collection<EncoreInteraction> interactions) {
		List<BinaryInteraction> convertedInteractions = new ArrayList<BinaryInteraction>(interactions.size());
		Iterator<EncoreInteraction> it = interactions.iterator();
		Encore2Binary converter = new Encore2Binary();
		while (it.hasNext()) {
			EncoreInteraction encoreInteraction = (EncoreInteraction) it.next();
			convertedInteractions.add(converter.getBinaryInteraction(encoreInteraction));
		}
		return convertedInteractions;
	}

	//Converts a Collection&lt;BinaryInteraction&lt;Interactor&gt;&gt; into a Collection&lt;EncoreInteraction&gt;
	public static Collection<EncoreInteraction> convertBinaryInteraction(Collection<BinaryInteraction> interactions) {
		List<EncoreInteraction> convertedInteractions = new ArrayList<EncoreInteraction>(interactions.size());
		Iterator<BinaryInteraction> it = interactions.iterator();
		Binary2Encore converter = new Binary2Encore();
		while (it.hasNext()) {
			BinaryInteraction binaryInteraction = (BinaryInteraction) it.next();
			convertedInteractions.add(converter.getEncoreInteraction(binaryInteraction));
		}
		return convertedInteractions;
	}

	//Retrieve only interactors from list of interactions
	public static Set<String> getInteractorsEncore(List<EncoreInteraction> interactions) {
		HashSet<String> interactors = new HashSet<String>();

		for (EncoreInteraction interaction : interactions) {
			if (interaction.getInteractorAccsA().containsKey("uniprotkb")) {
				String id = interaction.getInteractorAccsA().get("uniprotkb");
				if (ProteinUtils.UniProtId.isValid(id))
					interactors.add(id);
			}
			if (interaction.getInteractorAccsB().containsKey("uniprotkb")) {
				String id = interaction.getInteractorAccsB().get("uniprotkb");
				if (ProteinUtils.UniProtId.isValid(id))
					interactors.add(id);
			}
		}

		return interactors;
	}
	*/
}

package tk.nomis_tech.ppimapbuilder.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import tk.nomis_tech.ppimapbuilder.orthology.UniprotId;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLExpressionBuilder.Operator;
import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLParameterBuilder;
import uk.ac.ebi.enfin.mi.cluster.Binary2Encore;
import uk.ac.ebi.enfin.mi.cluster.Encore2Binary;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

import com.google.common.collect.Lists;

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

				System.out.println("N# proteins: " + sourceProteins.size());
				System.out.println("N# queries: " + NB_TRUNCATION);

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
						q.addCondition(Operator.AND, new MiQLParameterBuilder("idA", protsIdA));
						q.addCondition(Operator.AND, new MiQLParameterBuilder("idB", protsIdB));
						queries.add(q.toString());
						//System.out.println(q);
					}
				}
			} else {
				baseQuery.addCondition(Operator.AND, idA);
				baseQuery.addCondition(Operator.AND, idB);
				queries.add(baseQuery.toString());
			}
			System.gc();
		}

		//System.out.println(queries.size());

		// Executing all MiQL queries using ThreadedPsicquicSimpleClient
		List<BinaryInteraction> results = new ArrayList<BinaryInteraction>();
		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(services, 3);
		results.addAll(client.getByQueries(queries));
		
		return results;
	}

	/**
	 * Converts a Collection&lt;EncoreInteraction&gt; into a Collection&lt;BinaryInteraction&lt;Interactor&gt;&gt;
	 */
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
	
	/**
	 * Converts a Collection&lt;BinaryInteraction&lt;Interactor&gt;&gt; into a Collection&lt;EncoreInteraction&gt;
	 */
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
	public static Set<String> getInteractorsEncore(Collection<EncoreInteraction> interactions) {
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

		return interactors;
	}

	/**
	 * Retrieve only interactors from list of interactions
	 */
	public static List<String> getInteractorsBinary(Collection<BinaryInteraction> interactions, int refTaxId) {
		HashSet<String> interactors = new HashSet<String>();

		Iterator<BinaryInteraction> iterator = filterNonUniprotAndNonRefOrg(interactions, refTaxId).iterator();
		while (iterator.hasNext()) {
			BinaryInteraction interaction = iterator.next();

			interactors.add(interaction.getInteractorA().getIdentifiers().get(0).getIdentifier());
			interactors.add(interaction.getInteractorB().getIdentifiers().get(0).getIdentifier());
		}

		return new ArrayList<String>(interactors);
	}

	/**
	 * Remove all interaction from a list if at least one of the interactor doesn't have an "uniprotkb" identifier. Also sort the
	 * identifiers of interactor to make uniprotkb appear first
	 */
	public static Collection<BinaryInteraction> filterNonUniprotAndNonRefOrg(Collection<BinaryInteraction> interactions, int refTaxId) {
		List<BinaryInteraction> out = new ArrayList<BinaryInteraction>();

		Iterator<BinaryInteraction> it = interactions.iterator();
		while (it.hasNext()) {
			BinaryInteraction binaryInteraction = it.next();

			boolean ok = true;
			for (Interactor interactor : Arrays.asList(new Interactor[] { binaryInteraction.getInteractorA(),
					binaryInteraction.getInteractorB() })) {

				List<CrossReference> ids = interactor.getIdentifiers();
				// ids.addAll(interactor.getAlternativeIdentifiers());

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
				
				/*//TODO change taxID parameter => maybe split in two methods
				if(!interactor.getOrganism().getTaxid().equals(refTaxId+""))
					ok = false;*/

				List<CrossReference> sortedIdentifiers = new ArrayList<CrossReference>();
				ids.remove(uniprot);
				sortedIdentifiers.add(uniprot);
				sortedIdentifiers.addAll(ids);
				interactor.setIdentifiers(sortedIdentifiers);
			}

			if (ok) out.add(binaryInteraction);
		}

		return out;
	}
}

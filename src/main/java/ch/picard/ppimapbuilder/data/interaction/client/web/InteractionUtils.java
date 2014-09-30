package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

import java.util.*;

/**
 * Group of method useful for manipulation of interaction list
 */
public class InteractionUtils {



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
	public static Set<String> getInteractors(List<BinaryInteraction> interactions) {
		HashSet<String> interactors = new HashSet<String>();

		for (BinaryInteraction interaction : interactions) {
			String idA = ProteinUtils.UniProtId.extractStrictUniProtId(
					interaction.getInteractorA().getIdentifiers().get(0).getIdentifier()
			);
			String idB = ProteinUtils.UniProtId.extractStrictUniProtId(
					interaction.getInteractorB().getIdentifiers().get(0).getIdentifier()
			);

			if(idA != null && idB != null) {
				interactors.add(idA);
				interactors.add(idB);
			}
		}

		return interactors;
	}

	public static abstract class InteractionFilter {
		public boolean isValidInteraction(BinaryInteraction ignored) {
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
				boolean isUniprot = ref.getDatabase().equals("uniprotkb");
				boolean idValid = ProteinUtils.UniProtId.isValid(ref.getIdentifier());
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
	public static ArrayList<BinaryInteraction> filter(List<BinaryInteraction> interactions, InteractionFilter... filters) {
		ArrayList<BinaryInteraction> validInteractions = new ArrayList<BinaryInteraction>();
		interactionLoop : for (BinaryInteraction interaction : interactions) {
			boolean valid = true;

			for (InteractionFilter filter : filters) {
				valid = valid && filter.isValidInteraction(interaction);
				if(!valid) continue  interactionLoop;
			}

			for (Interactor interactor : new Interactor[]{interaction.getInteractorA(),
					interaction.getInteractorB()}) {

				for (InteractionFilter filter : filters) {
					valid = valid && filter.isValidInteractor(interactor);
					if(!valid) continue  interactionLoop;
				}
			}

			if(valid) validInteractions.add(interaction);
		}
		return validInteractions;
	}

	public static String generateMiQLQueryIDTaxID(final String id, final Integer taxId) {
		MiQLExpressionBuilder query = new MiQLExpressionBuilder();

		query.setRoot(true);
		query.add(new MiQLParameterBuilder("taxidA", taxId));
		query.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("taxidB", taxId));
		query.addCondition(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("id", id));

		return query.toString();
	}

	public static List<String> psicquicServicesToStrings(List<PsicquicService> services) {
		ArrayList<String> out = new ArrayList<String>();
		for(PsicquicService service : services) {
			out.add(service.getName());
		}
		return out;
	}

}

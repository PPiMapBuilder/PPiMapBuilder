package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

import java.util.*;
import java.util.concurrent.Callable;

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

	public static Protein getProteinInteractor(Interactor interactor) {
		String id = null;

		for (CrossReference reference : interactor.getIdentifiers()) {
			if (reference.getDatabase().equals("uniprotkb")) {
				id = reference.getIdentifier();
				break;
			}
		}

		Organism org = OrganismUtils.findOrganismInMITABTaxId(
				InParanoidOrganismRepository.getInstance(),
				interactor.getOrganism().getTaxid()
		);

		if (id != null && org != null)
			return new Protein(id, org);
		return null;
	}

	public static Pair<Protein> getInteractors(BinaryInteraction interaction) {
		Interactor interactorA = interaction.getInteractorA();
		Interactor interactorB = interaction.getInteractorB();

		return new Pair<Protein>(
				getProteinInteractor(interactorA),
				getProteinInteractor(interactorB)
		);
	}

	/**
	 * Retrieve only interactors from list of interactions
	 */
	public static HashSet<Protein> getInteractors(Collection<BinaryInteraction> interactions) {
		HashSet<Protein> interactors = new HashSet<Protein>();

		for (BinaryInteraction interaction : interactions) {
			final Pair<Protein> interactorPair = getInteractors(interaction);
			if (interactorPair.isNotNull()) {
				interactors.add(interactorPair.getFirst());
				interactors.add(interactorPair.getSecond());
			}
		}

		return interactors;
	}

	public static interface InteractionFilter {
		public boolean isValidInteraction(BinaryInteraction interaction);
	}

	public static abstract class InteractorFilter implements InteractionFilter {
		public abstract boolean isValidInteractor(Interactor interactor);

		@Override
		public boolean isValidInteraction(BinaryInteraction interaction) {
			Interactor interactorA = interaction.getInteractorA();
			Interactor interactorB = interaction.getInteractorB();
			return  isValidInteractor(interactorA)
					&& isValidInteractor(interactorB);
		}
	}

	public static final class OrganismInteractionFilter extends InteractorFilter {
		private final Organism organism;

		public OrganismInteractionFilter(Organism organism) {
			this.organism = organism;
		}

		@Override
		public boolean isValidInteractor(Interactor interactor) {
			return organism.equals(
					OrganismUtils.findOrganismInMITABTaxId(
							InParanoidOrganismRepository.getInstance(),
							interactor.getOrganism().getTaxid()
					)
			);
		}
	}

	public static final class UniProtInteractionFilter extends InteractorFilter {
		@Override
		public boolean isValidInteractor(Interactor interactor) {
			final List<CrossReference> ids = interactor.getIdentifiers();
			ids.addAll(interactor.getAlternativeIdentifiers());

			if (ids.size() == 1 && !ids.get(0).getDatabase().equals("uniprotkb"))
				return false;

			CrossReference uniprot = null;
			boolean hasUniprot = false;
			for (CrossReference ref : ids) {
				hasUniprot = hasUniprot || (
						ref.getDatabase().equals("uniprotkb") // Is UniProt
								&&
								ProteinUtils.UniProtId.isValid(ref.getIdentifier()) // Valid UniProt
				);
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

	public static boolean isValidInteraction(BinaryInteraction interaction, InteractionFilter... filters) {
		for (InteractionFilter filter : filters)
			if (!filter.isValidInteraction(interaction))
				return false;
		return true;
	}

	/**
	 * Filter a List of BinaryInteraction to keep only the interaction satisfying the filters InteractionFilter
	 *
	 * @param interactions
	 * @param filters
	 */
	public static ArrayList<BinaryInteraction> filter(List<BinaryInteraction> interactions, InteractionFilter... filters) {
		ArrayList<BinaryInteraction> validInteractions = new ArrayList<BinaryInteraction>();
		for (BinaryInteraction interaction : interactions)
			if (isValidInteraction(interaction, filters))
				validInteractions.add(interaction);
		return validInteractions;
	}

	public static ArrayList<BinaryInteraction> filterConcurrently(
			ExecutorServiceManager executorServiceManager,
			final List<BinaryInteraction> interactions,
			final ProgressMonitor progressMonitor,
			final InteractionFilter... filters
	) {
		final ArrayList<BinaryInteraction> validInteractions = new ArrayList<BinaryInteraction>();
		final double[] percent = new double[]{0d};
		final double size = interactions.size();
		new ConcurrentExecutor<Boolean>(executorServiceManager, interactions.size()) {
			@Override
			public Callable<Boolean> submitRequests(final int index) {
				return new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						if (progressMonitor != null) {
							double progress = Math.floor((index / size) * 100) / 100;
							if (progress > percent[0])
								progressMonitor.setProgress(percent[0] = progress);
						}
						return isValidInteraction(interactions.get(index), filters);
					}
				};
			}

			@Override
			public void processResult(Boolean result, Integer index) {
				if (result) validInteractions.add(interactions.get(index));
			}
		}.run();
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
		for (PsicquicService service : services) {
			out.add(service.getName());
		}
		return out;
	}

}

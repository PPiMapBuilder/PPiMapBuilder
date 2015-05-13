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

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.interaction.client.web.filter.InteractionFilter;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.ClusterServiceException;
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

	public static Collection<EncoreInteraction> clusterInteraction(final Iterator<BinaryInteraction> interactions)
			throws ClusterServiceException {
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

	public static <F extends InteractionFilter> InteractionFilter combineFilters(final F... filters) {
		return new InteractionFilter() {
			@Override
			public boolean isValidInteraction(BinaryInteraction interaction) {
				for (F filter : filters)
					if (!filter.isValidInteraction(interaction))
						return false;
				return true;
			}
		};
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
	@Deprecated
	public static ArrayList<BinaryInteraction> filter(List<BinaryInteraction> interactions, InteractionFilter... filters) {
		ArrayList<BinaryInteraction> validInteractions = new ArrayList<BinaryInteraction>();
		for (BinaryInteraction interaction : interactions)
			if (isValidInteraction(interaction, filters))
				validInteractions.add(interaction);
		return validInteractions;
	}

	@Deprecated
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
			public void processResult(Boolean intermediaryResult, Integer index) {
				if (intermediaryResult) validInteractions.add(interactions.get(index));
			}
		}.run();
		return validInteractions;
	}

	public static List<String> psicquicServicesToStrings(Collection<PsicquicService> services) {
		ArrayList<String> out = new ArrayList<String>();
		for (PsicquicService service : services) {
			out.add(service.getName());
		}
		return out;
	}

	public static List<PsicquicSimpleClient> psicquicServicesToPsicquicSimpleClients(Collection<PsicquicService> services) {
		final ArrayList<PsicquicSimpleClient> clients = new ArrayList<PsicquicSimpleClient>();
		for (PsicquicService service : services) {
			clients.add(new PsicquicSimpleClient(service.getRestUrl()));
		}
		return clients;
	}

	@Deprecated
	public static String generateMiQLQueryIDTaxID(final String id, final Integer taxId) {
		MiQLExpressionBuilder query = new MiQLExpressionBuilder();

		query.setRoot(true);
		query.add(new MiQLParameterBuilder("taxidA", taxId));
		query.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("taxidB", taxId));
		query.add(MiQLExpressionBuilder.Operator.AND, new MiQLParameterBuilder("id", id));

		return query.toString();
	}
}

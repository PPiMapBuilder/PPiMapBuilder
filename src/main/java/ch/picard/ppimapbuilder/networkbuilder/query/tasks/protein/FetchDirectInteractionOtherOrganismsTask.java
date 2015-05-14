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

package ch.picard.ppimapbuilder.networkbuilder.query.tasks.protein;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.ProgressTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.task.AbstractThreadedTask;
import org.cytoscape.work.TaskMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.*;
import java.util.concurrent.Callable;

class FetchDirectInteractionOtherOrganismsTask extends AbstractThreadedTask {

	private final Collection<PsicquicService> psicquicServices;

	// Input
	private final List<Organism> otherOrganisms;
	private final Organism referenceOrganism;
	private final Double MINIMUM_ORTHOLOGY_SCORE;

	private final Set<UniProtEntry> proteinOfInterestPool;
	// Output
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg;

	public FetchDirectInteractionOtherOrganismsTask(
			ExecutorServiceManager executorServiceManager,
			Collection<PsicquicService> psicquicServices, List<Organism> otherOrganisms, Organism referenceOrganism, Double minimum_orthology_score, Set<UniProtEntry> proteinOfInterestPool,
			UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<BinaryInteraction>> directInteractionsByOrg
	) {
		super(executorServiceManager);
		this.psicquicServices = psicquicServices;
		this.otherOrganisms = otherOrganisms;
		this.referenceOrganism = referenceOrganism;
		this.MINIMUM_ORTHOLOGY_SCORE = minimum_orthology_score;
		this.proteinOfInterestPool = proteinOfInterestPool;
		this.interactorPool = interactorPool;
		this.directInteractionsByOrg = directInteractionsByOrg;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetch direct interaction of input proteins orthologs in other organisms...");

		// Progress indicators
		final Map<Organism, Double> progressByOrganism = new HashMap<Organism, Double>();
		final Double[] progressPercent = new Double[]{0d};
		taskMonitor.setProgress(progressPercent[0] = 0d);

		final double size = otherOrganisms.size();
		new ConcurrentExecutor<PrimaryInteractionQuery>(executorServiceManager, otherOrganisms.size()) {

			@Override
			public Callable<PrimaryInteractionQuery> submitRequests(final int index) {
				final Organism organism = otherOrganisms.get(index);
				return new PrimaryInteractionQuery(
						executorServiceManager, psicquicServices, referenceOrganism, organism, proteinOfInterestPool, interactorPool,
						MINIMUM_ORTHOLOGY_SCORE,

						new ProgressTaskMonitor() {
							@Override
							public void setProgress(double currentProgress) {
								synchronized (progressPercent[0]) {
									final Double previousProgress = progressByOrganism.get(organism);
									progressByOrganism.put(organism, currentProgress);

									if (previousProgress != null && currentProgress - previousProgress > 1d / size) {
										double sum = 0d;
										for (Double progress : progressByOrganism.values())
											sum += progress;
										double percent = Math.floor(sum / size * 100) / 100;
										if (percent > progressPercent[0]) {
											taskMonitor.setProgress(progressPercent[0] = percent);
										}
									}
								}
							}
						}
				);
			}

			@Override
			public void processResult(PrimaryInteractionQuery intermediaryResult, Integer index) {
				directInteractionsByOrg.put(otherOrganisms.get(index), intermediaryResult.getNewInteractions());
			}

		}.run();

		if (progressPercent[0] < 1.0) taskMonitor.setProgress(1.0);
	}

}

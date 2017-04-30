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

package ch.picard.ppimapbuilder.networkbuilder.query.tasks.interactome;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntrySet;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

import java.util.Collection;
import java.util.HashMap;

@Deprecated
public class InteractomeNetworkQueryTaskFactory implements TaskFactory {
	private final ExecutorServiceManager executorServiceManager;
	private final Organism referenceOrganism;
	private final UniProtEntrySet interactorPool;
	private final HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg;
	private final Collection<PsicquicService> psicquicServices;

	public InteractomeNetworkQueryTaskFactory(
			ExecutorServiceManager executorServiceManager,
			Collection<PsicquicService> psicquicServices, Organism referenceOrganism, UniProtEntrySet interactorPool,
			HashMap<Organism, Collection<EncoreInteraction>> interactionsByOrg
	) {
		this.executorServiceManager = executorServiceManager;
		this.psicquicServices = psicquicServices;
		this.referenceOrganism = referenceOrganism;
		this.interactorPool = interactorPool;
		this.interactionsByOrg = interactionsByOrg;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new FetchInteractionsTask(
						executorServiceManager,
						interactorPool, interactionsByOrg, psicquicServices,
						referenceOrganism
				)
		);
	}

	@Override
	public boolean isReady() {
		return true;
	}
}

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

package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.interaction.Interaction;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import ch.picard.ppimapbuilder.networkbuilder.query.PMBInteractionQueryTaskFactory;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import java.util.*;

/**
 * PPiMapBuilder network query and build
 */
public class PMBInteractionNetworkBuildTaskFactory extends AbstractTaskFactory {

	// Cytoscape services
	private final CyNetworkManager networkManager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkNaming networkNaming;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkViewManager networkViewManager;
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;
	private final VisualMappingManager visualMappingManager;

	// Data input from the user
	private final NetworkQueryParameters networkQueryParameters;

	// Data output from network querying
	private Set<UniProtEntry> proteinOfInterestPool; // not the same as user input
	private Collection<Interaction> interactions;

	// Error output
	private String errorMessage;

	public PMBInteractionNetworkBuildTaskFactory(
			final CyNetworkManager networkManager,
			final CyNetworkNaming networkNaming,
			final CyNetworkFactory networkFactory,
			final CyNetworkViewFactory networkViewFactory,
			final CyNetworkViewManager networkViewManager,
			final CyLayoutAlgorithmManager layoutAlgorithmManager,
			final VisualMappingManager visualMappingManager,
			final NetworkQueryParameters networkQueryParameters
	) {
		this.networkManager = networkManager;
		this.networkNaming = networkNaming;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.networkViewManager = networkViewManager;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkQueryParameters = networkQueryParameters;

		this.errorMessage = null;
	}

	@Override
	public TaskIterator createTaskIterator() {
		long startTime = System.currentTimeMillis();

		this.interactions = new ArrayList<Interaction>();
		this.proteinOfInterestPool = new HashSet<UniProtEntry>();

		ExecutorServiceManager executorServiceManager =
				new ExecutorServiceManager((Runtime.getRuntime().availableProcessors() + 1) * 3);

		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(
				new PMBInteractionQueryTaskFactory(
						interactions,
						proteinOfInterestPool,
						networkQueryParameters,
						executorServiceManager
				)
		);
		taskIterator.append(
				new PMBCreateNetworkTask(
						networkManager, networkNaming, networkFactory, networkViewFactory, networkViewManager,
						layoutAlgorithmManager, visualMappingManager, interactions,
						proteinOfInterestPool, networkQueryParameters, executorServiceManager, startTime
				)
		);
		return taskIterator;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	protected Collection<Interaction> getInteractions() {
		return interactions;
	}

	protected Set<UniProtEntry> getProteinOfInterestPool() {
		return proteinOfInterestPool;
	}
}

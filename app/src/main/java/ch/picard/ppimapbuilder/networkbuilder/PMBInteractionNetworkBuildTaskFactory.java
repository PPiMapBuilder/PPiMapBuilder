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

import ch.picard.ppimapbuilder.PPiQueryService;
import ch.picard.ppimapbuilder.data.interaction.Interaction;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.networkbuilder.network.PMBCreateNetworkTask;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(
				new Task() {
					@Override
					public void run(TaskMonitor taskMonitor) throws Exception {
					    List<Map> dbs = networkQueryParameters.getSelectedDatabases();
						List<String> dbNames = new ArrayList<String>();
						for (Map db : dbs) {
							dbNames.add(db.get("name").toString());
						}
                        long taxId = networkQueryParameters.getReferenceOrganism().getTaxId();

                        if (networkQueryParameters.isInteractomeQuery()) {
                            List interactions = PPiQueryService.getInstance().getInteractome(dbNames, taxId);
                        } else {
                            List<Organism> otherOrganisms = networkQueryParameters.getOtherOrganisms();
                            List<Long> otherOrganismIds = new ArrayList<Long>();
                            for (Organism otherOrganism : otherOrganisms) {
                                otherOrganismIds.add(((long) otherOrganism.getTaxId()));
                            }
                            List<String> proteinIds = networkQueryParameters.getProteinOfInterestUniprotId();
                            List its = PPiQueryService.getInstance().getProteinNetwork(dbNames, taxId, proteinIds, otherOrganismIds);
                            interactions.addAll(convertInteractions(its, networkQueryParameters.getReferenceOrganism()));
                        }
					}

					@Override
					public void cancel() {

					}
				}
		);
		taskIterator.append(
				new PMBCreateNetworkTask(
						networkManager, networkNaming, networkFactory, networkViewFactory, networkViewManager,
						layoutAlgorithmManager, visualMappingManager, interactions,
                        networkQueryParameters, startTime
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

	public static List<Interaction> convertInteractions(Object interactionMaps, Organism refOrganism) {
	    List<Interaction> interactions = new ArrayList<Interaction>();
        for (Object interactionMap : ((List) interactionMaps)) {
            Map interactionMap1 = (Map) interactionMap;
            Map protA = (Map) interactionMap1.get("protein-a");
            Organism orgA = new Organism((Map) protA.get("organism"));
            Map protB = (Map) interactionMap1.get("protein-b");
            Organism orgB = new Organism((Map) protB.get("organism"));

            Protein proteinA = new Protein((String) protA.get("uniprotid"), orgA);
            Protein proteinB = new Protein((String) protB.get("uniprotid"), orgB);
            Interaction interaction = new Interaction(proteinA, proteinB, null, refOrganism);
            interactions.add(interaction);
        }
        return interactions;
    }
}

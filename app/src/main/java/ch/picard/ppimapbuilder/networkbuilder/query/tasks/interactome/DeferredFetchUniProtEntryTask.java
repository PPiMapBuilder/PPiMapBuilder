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

import ch.picard.ppimapbuilder.data.JSONUtils;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyCategory;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.client.web.UniProtEntryClient;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.*;
import java.util.concurrent.Callable;

public class DeferredFetchUniProtEntryTask extends AbstractTask {

	private final ExecutorServiceManager executorServiceManager;
	private final Set<? extends Protein> interactorPool;

	private ConcurrentExecutor concurrentExecutor = null;

	private final CyNetwork network;

	public DeferredFetchUniProtEntryTask(
			ExecutorServiceManager executorServiceManager,
			Set<? extends Protein> interactorPool,
			CyNetwork network
	) {
		this.executorServiceManager = executorServiceManager;
		this.interactorPool = interactorPool;
		this.network = network;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Fetching UniProt entries...");

		//Index node
		final Map<String, CyNode> nodes = new HashMap<String, CyNode>();
		for (CyNode node : network.getNodeList()) {
			nodes.put(
					network.getRow(node).get("Uniprot_id", String.class),
					node
			);
		}

		// Get list of protein UniProt identifiers
		final List<String> uniProtIds =
				new ArrayList<String>(ProteinUtils.asIdentifiers(interactorPool));

		// Fetch UniProt entries
		final HashMap<String, UniProtEntry> entries = new HashMap<String, UniProtEntry>();
		new ConcurrentExecutor<UniProtEntry>(executorServiceManager, uniProtIds.size()) {
			UniProtEntryClient uniProtClient = new UniProtEntryClient(executorServiceManager);
			double progress = 0d;

			@Override
			public Callable<UniProtEntry> submitRequests(final int index) {
				return new Callable<UniProtEntry>() {
					@Override
					public UniProtEntry call() throws Exception {
						return uniProtClient.retrieveProteinData(uniProtIds.get(index));
					}
				};
			}

			@Override
			public void processResult(UniProtEntry entry, Integer index) {
				taskMonitor.setProgress(++progress / (double) uniProtIds.size());
				entries.put(uniProtIds.get(index), entry);

				if(entries.size() > 1000) {
					// Update CyTable
					updateCyTable(entries, nodes);
				}
			}
		}.run();

		updateCyTable(entries, nodes);
	}

	private void updateCyTable(Map<String, UniProtEntry> entries, Map<String, CyNode> nodes) {
		for (String uniProtId : entries.keySet()) {
			final CyNode node = nodes.get(uniProtId);
			final UniProtEntry entry = entries.get(uniProtId);
			if(node != null && entry != null) {
				final CyRow row = network.getRow(node);
				final GeneOntologyTermSet geneOntologyTerms = entry.getGeneOntologyTerms();

				row.set("Accessions", new ArrayList<String>(entry.getAccessions()));
				row.set("Gene_name", entry.getGeneName());
				row.set("Ec_number", entry.getEcNumber());
				row.set("Synonym_gene_names", new ArrayList<String>(entry.getSynonymGeneNames()));
				row.set("Protein_name", entry.getProteinName());
				row.set("Tax_id", String.valueOf(entry.getOrganism().getTaxId()));
				row.set("Reviewed", String.valueOf(entry.isReviewed()));

				final GeneOntologyTermSet cellularComponent =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.CELLULAR_COMPONENT);
				row.set("Cellular_components_hidden",
						JSONUtils.jsonListToStringList(cellularComponent)
				);
				row.set("Cellular_components", cellularComponent.asStringList());

				final GeneOntologyTermSet biologicalProcess =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.BIOLOGICAL_PROCESS);
				row.set("Biological_processes_hidden",
						JSONUtils.jsonListToStringList(biologicalProcess)
				);
				row.set("Biological_processes", biologicalProcess.asStringList());

				final GeneOntologyTermSet molecularFunction =
						geneOntologyTerms.getByCategory(GeneOntologyCategory.MOLECULAR_FUNCTION);
				row.set("Molecular_functions_hidden",
						JSONUtils.jsonListToStringList(molecularFunction)
				);
				row.set("Molecular_functions", molecularFunction.asStringList());

				for (CyEdge cyEdge : network.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
					final CyRow edgeRow = network.getRow(cyEdge);
					if(cyEdge.getSource().equals(node)) {
						edgeRow.set("Protein_name_A", entry.getProteinName());
						edgeRow.set("Gene_name_A", entry.getGeneName());
					}
					else if(cyEdge.getTarget().equals(node)) {
						edgeRow.set("Protein_name_B", entry.getProteinName());
						edgeRow.set("Gene_name_B", entry.getGeneName());
					}
				}
			}
		}

		entries.clear();
	}

	@Override
	public void cancel() {
		super.cancel();
		if (concurrentExecutor != null)
			concurrentExecutor.cancel();
	}
}

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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.*;
import java.util.concurrent.Callable;

public class DifferedFetchUniProtEntryTask extends AbstractTask {

	private final ExecutorServiceManager executorServiceManager;
	private final Set<? extends Protein> interactorPool;

	private ConcurrentExecutor concurrentExecutor = null;

	private final CyNetwork network;

	public DifferedFetchUniProtEntryTask(
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

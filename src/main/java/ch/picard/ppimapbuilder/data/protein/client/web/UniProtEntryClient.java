package ch.picard.ppimapbuilder.data.protein.client.web;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.util.IOUtils;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Simple Java client for UniProt entry service
 */
//TODO : rewrite client with Stax XML Parser
public class UniProtEntryClient extends AbstractThreadedClient {

	private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

	public UniProtEntryClient(ExecutorServiceManager executorServiceManager) {
		super(executorServiceManager);
	}

	/**
	 * Retrieve UniProt entry of a protein
	 */
	public UniProtEntry retrieveProteinData(String uniprotId) throws IOException {
		return new RetrieveProteinData(uniprotId).call().protein;
	}

	/**
	 * Retrieves UniProt entry data of a list of protein using threaded execution pool
	 */
	public HashMap<String, UniProtEntry> retrieveProteinsData(final Collection<String> uniProtIds) {
		return retrieveProteinsData(uniProtIds, null);
	}

	/**
	 * Retrieves UniProt entry data of a list of protein using threaded execution pool
	 */
	public HashMap<String, UniProtEntry> retrieveProteinsData(final Collection<String> uniProtIds, final ProgressMonitor progressMonitor) {
		final List<String> proteinArray = new ArrayList<String>(new HashSet<String>(uniProtIds));

		final HashMap<String, UniProtEntry> results = new HashMap<String, UniProtEntry>();
		final double[] progress = new double[]{0d};
		new ConcurrentExecutor<RetrieveProteinData>(getExecutorServiceManager(), uniProtIds.size()) {

			@Override
			public Callable<RetrieveProteinData> submitRequests(int index) {
				return new RetrieveProteinData(proteinArray.get(index));
			}

			@Override
			public void processResult(RetrieveProteinData result, Integer index) {
				progressMonitor.setProgress(++progress[0]/uniProtIds.size());
				results.put(result.originalUniProtId, result.protein);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				e.getCause().printStackTrace();
				return false;
			}

		}.run();

		return results;
	}

	private class RetrieveProteinData implements Callable<RetrieveProteinData> {

		//Input
		final String originalUniProtId;

		//Output
		UniProtEntry protein = null;

		private RetrieveProteinData(String originalUniProtId) {
			this.originalUniProtId = originalUniProtId;
		}

		@Override
		public RetrieveProteinData call() throws IOException {
			Document doc = IOUtils.getDocumentWithRetry(UNIPROT_URL + originalUniProtId + ".xml", 1200, 1000, 7, 100);

			if (doc == null || doc.select("body").get(0).childNodeSize() < 1)
				return this;

			UniProtEntry.Builder builder = new UniProtEntry.Builder();

			// ACCESSIONS
			boolean first = true;
			for (Element e : doc.select("accession")) {
				if (first) {
					builder.setUniprotId(e.text());
					first = false;
				}
				builder.addAccession(e.text());
			}
			builder.addAccession(originalUniProtId);

			// ORGANISM
			for (Element e : doc.select("organism")) {
				String scientificName = "";
				for (Element name : e.select("name")) {
					if (name.attr("type").equals("scientific")) {
						scientificName = name.text();
						break;
					}
				}
				builder.setOrganism(new Organism(
						scientificName,
						Integer.valueOf(e.select("dbReference").attr("id"))
				));
				break;
			}

			// GENE NAME AND SYNONYMS
			for (Element e : doc.select("gene")) {
				for (Element f : e.select("name")) {
					if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
						builder.setGeneName(f.text());
					} else { // Else, we organism the names as synonyms
						builder.addSynonymGeneName(f.text());
					}
				}
			}

			// PROTEIN NAME
			for (Element e : doc.select("protein")) {
				if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
					builder.setProteinName(e.select("recommendedName").select("fullName").text());
				} else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exists, we take the submitted name (usually from TrEMBL but not always)
					builder.setProteinName(e.select("submittedName").select("fullName").text());
				}
				break;
			}

			// REVIEWED
			for (Element e : doc.select("entry")) {
				builder.setReviewed(e.attr("dataset").equalsIgnoreCase("Swiss-Prot")); // If the protein comes from Swiss-Prot, it is reviewed
				break;
			}

			// EC NUMBER
			for (Element e : doc.select("ecNumber")) {
				builder.setEcNumber(e.text());
				break;
			}

			// GENE ONTOLOGIES
			for (Element e : doc.select("dbReference")) {
				if (e.attr("type").equals("GO")) {
					String id = e.attr("id");

					for (Element f : e.select("property")) {
						if (f.attr("type").equals("term")) {
							String[] values = f.attr("value").split(":");

							builder.addGeneOntologyTerm(new GeneOntologyTerm(id, values[1], values[0].charAt(0)));
							break;
						}
					}
				}
			}

			//System.out.println("uniprotEntryClient:"+protein.getUniProtId()+":"+pos+"try-ok");

			// PROTEIN CREATION
			protein = builder.build();
			return this;
		}
	}
}

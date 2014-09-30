package ch.picard.ppimapbuilder.data.protein.client.web;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.util.ConcurrentExecutor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Simple Java client for UniProt entry service
 */
//TODO : rewrite client with Stax XML Parser
public class UniProtEntryClient extends AbstractThreadedClient {

	private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

	public UniProtEntryClient() {
		super();
	}

	public UniProtEntryClient(int nbThread) {
		super(nbThread);
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
		final List<String> proteinArray = new ArrayList<String>(new HashSet<String>(uniProtIds));

		final HashMap<String, UniProtEntry> results = new HashMap<String, UniProtEntry>();
		new ConcurrentExecutor<RetrieveProteinData>(newThreadPool(), uniProtIds.size()) {

			@Override
			public Callable<RetrieveProteinData> submitRequests(int index) {
				return new RetrieveProteinData(proteinArray.get(index));
			}

			@Override
			public void processResult(RetrieveProteinData result, Integer index) {
				results.put(result.protein.getUniProtId(), result.protein);
			}

			@Override
			public boolean processExecutionException(ExecutionException e, Integer index) {
				e.getCause().printStackTrace();
				return false;
			}

		}.run();
		return results;
	}

	private static class RetrieveProteinData implements Callable<RetrieveProteinData> {

		//Input
		final String uniProtId;

		//Output
		UniProtEntry protein = null;

		private RetrieveProteinData(String uniProtId) {
			this.uniProtId = uniProtId;
		}

		@Override
		public RetrieveProteinData call() throws IOException {
			Document doc = null;
			final int MAX_TRY = 4;
			int pos = 0;
			IOException lastError = null;
			do {
				try {
					Connection connect = Jsoup.connect(UNIPROT_URL + uniProtId + ".xml");
					connect.timeout(6000);
					doc = connect.get();
				} catch (HttpStatusException e) {
					if (e.getStatusCode() == 404) return null;//No protein entry found
					lastError = new IOException(e);
				} catch (SocketTimeoutException e) {
					lastError = new IOException(e);
				}
			} while (doc == null && ++pos < MAX_TRY);
			if (doc == null) throw lastError;

			Organism organism = null;
			String geneName = null;
			ArrayList<String> synonymGeneNames = new ArrayList<String>();
			String proteinName = null;
			String ec_number = null;
			boolean reviewed = false;

			// ORGANISM
			for (Element e : doc.select("organism")) {
				String scientificName = "";
				for(Element name: e.select("name")) {
					if(name.attr("type").equals("scientific")) {
						scientificName = name.text();
						break;
					}
				}
				organism = new Organism(
					scientificName,
					Integer.valueOf(e.select("dbReference").attr("id"))
				);
				break;
			}

			// GENE NAME AND SYNONYMS
			for (Element e : doc.select("gene")) {
				for (Element f : e.select("name")) {
					if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
						geneName = f.text();
					} else { // Else, we organism the names as synonyms
						synonymGeneNames.add(f.text());
					}
				}
			}

			// PROTEIN NAME
			for (Element e : doc.select("protein")) {
				if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
					proteinName = e.select("recommendedName").select("fullName").text();
				} else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exist, we take the submitted name (usually from TrEMBL but not always)
					proteinName = e.select("submittedName").select("fullName").text();
				}
				break;
			}

			// REVIEWED
			for (Element e : doc.select("entry")) {
				reviewed = e.attr("dataset").equalsIgnoreCase("Swiss-Prot"); // If the protein comes from Swiss-Prot, it is reviewed
				break;
			}

			// EC NUMBER
			for (Element e : doc.select("ecNumber")) {
				ec_number = e.text();
				break;
			}

			// GENE ONTOLOGIES
			Set<GeneOntologyTerm> biologicalProcesses = new HashSet<GeneOntologyTerm>();
			Set<GeneOntologyTerm> cellularComponents = new HashSet<GeneOntologyTerm>();
			Set<GeneOntologyTerm> molecularFunctions = new HashSet<GeneOntologyTerm>();
			for (Element e : doc.select("dbReference")) {
				if (e.attr("type").equals("GO")) {
					String id = e.attr("id");

					for (Element f : e.select("property")) {
						if (f.attr("type").equals("term")) {
							String[] values = f.attr("value").split(":");

							GeneOntologyTerm go = new GeneOntologyTerm(id, values[1], values[0].charAt(0));
							switch (go.getCategory()) {
								case BIOLOGICAL_PROCESS :
									biologicalProcesses.add(go);
									break;
								case CELLULAR_COMPONENT :
									cellularComponents.add(go);
									break;
								case MOLECULAR_FUNCTION :
									molecularFunctions.add(go);
									break;
							}
							break;
						}
					}
				}
			}

			//System.out.println("uniprotEntryClient:"+protein.getUniProtId()+":"+pos+"try-ok");

			// PROTEIN CREATION
			protein = new UniProtEntry(
					uniProtId,
					geneName,
					ec_number,
					organism,
					proteinName,
					reviewed,
					synonymGeneNames,
					biologicalProcesses,
					cellularComponents,
					molecularFunctions
			);
			return this;
		}
	}
}

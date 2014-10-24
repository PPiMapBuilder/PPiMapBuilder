package ch.picard.ppimapbuilder.data.protein.client.web;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
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

	private final DefaultHttpRequestRetryHandler retryHandler;
	private final RequestConfig requestConfig;

	public UniProtEntryClient(ExecutorServiceManager executorServiceManager) {
		super(executorServiceManager);

		requestConfig = RequestConfig.custom()
				.setSocketTimeout(1000)
				.setConnectTimeout(1000)
				.setConnectionRequestTimeout(1000)
				.build();
		retryHandler = new DefaultHttpRequestRetryHandler(1, true);
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
		new ConcurrentExecutor<RetrieveProteinData>(getExecutorServiceManager(), uniProtIds.size()) {

			@Override
			public Callable<RetrieveProteinData> submitRequests(int index) {
				return new RetrieveProteinData(proteinArray.get(index));
			}

			@Override
			public void processResult(RetrieveProteinData result, Integer index) {
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

		private CloseableHttpClient httpClient;

		//Input
		final String originalUniProtId;

		//Output
		UniProtEntry protein = null;

		private RetrieveProteinData(String originalUniProtId) {
			this.originalUniProtId = originalUniProtId;
			this.httpClient = HttpClients.custom()
					.setRetryHandler(retryHandler)
					.setDefaultRequestConfig(requestConfig)
					.build();
		}

		public Document getDocumentRetry() throws IOException {
			HttpRequestBase req = null;
			CloseableHttpResponse res = null;
			String url = UNIPROT_URL + originalUniProtId + ".xml";

			final int MAX_TRY = 5;
			int tryCount = 0;
			IOException error = null;
			while(tryCount <= MAX_TRY) {
				if(tryCount > 0) {
					httpClient = HttpClients.custom()
							.setRetryHandler(retryHandler)
							.setDefaultRequestConfig(RequestConfig.custom()
									.setSocketTimeout(1000 + tryCount * 800)
									.setConnectTimeout(1000 + tryCount * 800)
									.setConnectionRequestTimeout(1000 + tryCount * 800)
									.build())
							.build();
				}
				tryCount++;

				try {
					req = new HttpGet(url);
					res = httpClient.execute(req);

					int statusCode = res.getStatusLine().getStatusCode();

					if (200 <= statusCode && statusCode < 300) {
						return Jsoup.parse(
								res.getEntity().getContent(),
								"UTF-8",
								"/"
						);
					}
				} catch (IOException e) {
					error = new IOException(e.getMessage() + " " + url);
				} finally {
					if (res != null) res.close();
					if (req != null) req.releaseConnection();
				}
			}
			if(error != null) throw error;
			return null;
		}

		@Override
		public RetrieveProteinData call() throws IOException {
			Document doc = getDocumentRetry();

			if (doc == null || doc.select("body").get(0).childNodeSize() < 1) {
				return this;
			}

			String uniprotId = null;
			LinkedHashSet<String> accessions = new LinkedHashSet<String>();
			Organism organism = null;
			String geneName = null;
			HashSet<String> synonymGeneNames = new HashSet<String>();
			String proteinName = null;
			String ec_number = null;
			boolean reviewed = false;

			// ACCESSIONS
			boolean first = true;
			for (Element e : doc.select("accession")) {
				if(first) {
					uniprotId = e.text();
					first = false;
				}
				accessions.add(e.text());
			}
			accessions.add(originalUniProtId);

			// ORGANISM
			for (Element e : doc.select("organism")) {
				String scientificName = "";
				for (Element name : e.select("name")) {
					if (name.attr("type").equals("scientific")) {
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
				} else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exists, we take the submitted name (usually from TrEMBL but not always)
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
								case BIOLOGICAL_PROCESS:
									biologicalProcesses.add(go);
									break;
								case CELLULAR_COMPONENT:
									cellularComponents.add(go);
									break;
								case MOLECULAR_FUNCTION:
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
					uniprotId,
					accessions,
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

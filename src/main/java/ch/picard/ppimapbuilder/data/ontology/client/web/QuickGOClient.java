package ch.picard.ppimapbuilder.data.ontology.client.web;

import au.com.bytecode.opencsv.CSVReader;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.protein.Protein;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A simple implementation of a Java client for the QuickGO web service : http://www.ebi.ac.uk/QuickGO/ .<br/>
 * Currently only implements an interface to the GAnnotation service for generation list of slimmed GO terms for a set of protein.
 */
public class QuickGOClient {

	protected static final int MAX_URL_LENGTH = 4000; // Apache client max URL length

	private QuickGOClient() {}

	static public class GOSlimClient {
		/**
		 * Uses the "GAnnotation" service to retrieved a list of slimmed GO terms for a set of protein.
		 *
		 * @param GOSlimTerms a Set of GeneOntology term used as a GO slim
		 * @param proteinSet  a Set of Protein
		 */
		public HashMap<Protein, Set<GeneOntologyTerm>> searchProteinGOSlim(Set<GeneOntologyTerm> GOSlimTerms, Set<Protein> proteinSet) {
			final HashMap<Protein, Set<GeneOntologyTerm>> out = new HashMap<Protein, Set<GeneOntologyTerm>>();

			CloseableHttpClient httpClient = null;
			HttpRequestBase request = null;
			CloseableHttpResponse response = null;

			try {
				// Construct request URL
				for (URI requestURL : generateRequests(
						new ArrayList<GeneOntologyTerm>(GOSlimTerms),
						new ArrayList<Protein>(proteinSet)
					)
				) {
					System.out.println(requestURL.toString());
					System.out.println("Request length : " + requestURL.toString().length());

					// Prepare HTTP client, request and response
					httpClient = HttpClientBuilder.create().build();
					request = new HttpGet(requestURL);
					response = httpClient.execute(request);

					int statusCode = response.getStatusLine().getStatusCode();

					if (200 <= statusCode && statusCode < 300) {
						final InputStreamReader input = new InputStreamReader(response.getEntity().getContent());

						CSVReader reader = new CSVReader(input, '\t', '"', 1);

						String[] line;

						// Parse TSV response and create the HashMap output
						while ((line = reader.readNext()) != null) {
							if (line[0].isEmpty())
								break;

							final GeneOntologyTerm GOTerm = new GeneOntologyTerm(line[1], line[2], line[3].charAt(0));

							for (Protein protein : proteinSet) {
								if (protein.getUniProtId().equals(line[0])) {
									Set<GeneOntologyTerm> ontologyTerms = out.get(protein);
									if (ontologyTerms == null) {
										ontologyTerms = new HashSet<GeneOntologyTerm>();
										out.put(protein, ontologyTerms);
									}
									ontologyTerms.add(GOTerm);
									break;
								}
							}
						}
					}
				}

			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (response != null)
						response.close();
					if (request != null)
						request.releaseConnection();
					if (httpClient != null)
						httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return out;
		}


		/**
		 * Generates request URLs so that they can not exceed the URL maximum length.
		 */
		protected static List<URI> generateRequests(List<GeneOntologyTerm> GOSlimTerms, List<Protein> proteinSet) throws URISyntaxException {
			final List<URI> out = new ArrayList<URI>();

			final int GOTermListSize = GOSlimTerms.size();
			final int ProteinListSize = proteinSet.size();

			final int BASE_URL_LENGTH = 130; // Gross estimation

			int numberOfGOTermsSubLists = 1;
			int numberOfProteinSubLists = 1;
			int estimatedLength;

			int GOTermsStepLength;
			int ProteinStepLength;

			// Check number of requests needed to
			do {
				GOTermsStepLength = (int) Math.ceil((double) GOTermListSize / numberOfGOTermsSubLists);
				ProteinStepLength = (int) Math.ceil((double) ProteinListSize / numberOfProteinSubLists);

				estimatedLength = BASE_URL_LENGTH +
						GOTermsStepLength * GeneOntologyTerm.TERM_LENGTH +
						ProteinStepLength * Protein.ID_LENGTH;

				if(estimatedLength > MAX_URL_LENGTH) {
					if(GOTermsStepLength > ProteinStepLength)
						numberOfGOTermsSubLists++;
					else if(ProteinStepLength > GOTermsStepLength)
						numberOfProteinSubLists++;
					else {
						numberOfGOTermsSubLists++;
						numberOfProteinSubLists++;
					}
				}

			} while (estimatedLength > MAX_URL_LENGTH);

			int numberOfRequests = Math.max(numberOfGOTermsSubLists, numberOfProteinSubLists);
			//System.out.println("NB: " + numberOfRequests);

			// Generate list of list of GO Terms and Protein
			final List<List<GeneOntologyTerm>> subListOfGOTerms = new ArrayList<List<GeneOntologyTerm>>();
			final List<List<Protein>> subListOfProtein = new ArrayList<List<Protein>>();
			{
				int posInProteinSet = 0;
				int posInGOtermsSet = 0;
				boolean protDone = false;
				boolean GODone = false;
				for (int i = 0; i < numberOfRequests; i++) {

					if (!GODone) {
						int fromInGOtermsSet = posInGOtermsSet;
						int toInGOtermsSet = Math.min(fromInGOtermsSet + GOTermsStepLength, GOTermListSize);

						//System.out.println("from:  "+fromInGOtermsSet+ " - to: "+toInGOtermsSet);
						subListOfGOTerms.add(GOSlimTerms.subList(fromInGOtermsSet, toInGOtermsSet));

						posInGOtermsSet = toInGOtermsSet;
						if (toInGOtermsSet == GOSlimTerms.size())
							GODone = true;
					}

					if (!protDone) {
						int fromInProteinSet = posInProteinSet;
						int toInProteinSet = Math.min(fromInProteinSet + ProteinStepLength, ProteinListSize);

						//System.out.println("from:  "+fromInProteinSet+ " - to: "+toInProteinSet);

						subListOfProtein.add(proteinSet.subList(fromInProteinSet, toInProteinSet));

						posInProteinSet = toInProteinSet;
						if (toInProteinSet == proteinSet.size())
							protDone = true;
					}


				}
			}

			//Create all combinations of subListOfGOTerms and subListOfProtein
			// => Form a request URL
			for (List<GeneOntologyTerm> goTerms : subListOfGOTerms) {
				for (List<Protein> protein : subListOfProtein) {
					out.add(new URIBuilder()
									.setScheme("http")
									.setHost("www.ebi.ac.uk")
									.setPath("/QuickGO/GAnnotation")
									.addParameter("format", "tsv")
									.addParameter("col", "proteinID,goID,goName,aspect")
									.addParameter("termUse", "slim")
									.addParameter("limit", "-1")
									.addParameter("goid", StringUtils.join(goTerms, ","))
									.addParameter("protein", StringUtils.join(protein, ","))
									.build()
					);
				}
			}

			return out;
		}
	}
}

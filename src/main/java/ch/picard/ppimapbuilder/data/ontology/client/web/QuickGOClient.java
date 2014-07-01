package ch.picard.ppimapbuilder.data.ontology.client.web;

import au.com.bytecode.opencsv.CSVReader;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.OntologyTerm;
import ch.picard.ppimapbuilder.data.protein.Protein;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple implementation of a Java client for the QuickGO web service : http://www.ebi.ac.uk/QuickGO/
 */
public class QuickGOClient {

	/**
	 * Uses the "GAnnotation" service to retrieved a slimmed GO for a protein Set
	 *
	 * @param GOSlimTerms a Set of GeneOntology term used as a GO slim
	 * @param proteinSet  a Set of Protein
	 */
	public HashMap<Protein, Set<GeneOntologyTerm>> getSlimmedTermList(Set<OntologyTerm> GOSlimTerms, Set<Protein> proteinSet) {
		final HashMap<Protein, Set<GeneOntologyTerm>> out = new HashMap<Protein, Set<GeneOntologyTerm>>();

		CloseableHttpClient httpClient = null;
		HttpRequestBase request = null;
		CloseableHttpResponse response = null;

		try {
			// Construct request URL
			final URI requestURL = new URIBuilder()
					.setScheme("http")
					.setHost("www.ebi.ac.uk")
					.setPath("/QuickGO/GAnnotation")
					.addParameter("format", "tsv")
					.addParameter("col", "proteinID,goID,goName,aspect")
					.addParameter("termUse", "slim")
					.addParameter("gz", "")
					.addParameter("goid", setToString(GOSlimTerms, new Stringifier<OntologyTerm>() {
						@Override
						public String stringify(OntologyTerm term) {
							return term.getIdentifier();
						}
					}))
					.addParameter("protein", setToString(proteinSet, new Stringifier<Protein>() {
						@Override
						public String stringify(Protein object) {
							return object.getUniProtId();
						}
					}))
					.build();

			System.out.println(requestURL);

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
					if(line[0].isEmpty())
						break;

					final GeneOntologyTerm GOTerm = new GeneOntologyTerm(line[1], line[2], line[3].charAt(0));

					for (Protein protein : proteinSet) {
						if(protein.getUniProtId().equals(line[0])) {
							Set<GeneOntologyTerm> ontologyTerms = out.get(protein);
							if(ontologyTerms == null) {
								ontologyTerms = new HashSet<GeneOntologyTerm>();
								out.put(protein, ontologyTerms);
							}
							ontologyTerms.add(GOTerm);
							break;
						}
					}

					for (String s : line)
						System.out.print(s + "\t");
					System.out.println();
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

	private static <T> String setToString(Set<T> objects, Stringifier<T> stringifier) {
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (T object : objects) {
			if (!first)
				out.append(",");
			else
				first = false;

			out.append(stringifier.stringify(object));
		}
		return out.toString();
	}

	interface Stringifier<T> {
		public String stringify(T object);
	}
}

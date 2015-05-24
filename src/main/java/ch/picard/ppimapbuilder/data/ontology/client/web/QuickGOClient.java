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
    
package ch.picard.ppimapbuilder.data.ontology.client.web;

import au.com.bytecode.opencsv.CSVReader;
import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import ch.picard.ppimapbuilder.util.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * A simple implementation of a Java client for the QuickGO web service : http://www.ebi.ac.uk/QuickGO/ .<br/>
 * Currently only implements an interface to the GAnnotation service for generation list of slimmed GO terms for a set of protein.
 */
public class QuickGOClient {

	protected static final int MAX_URL_LENGTH = 2000; // client max URL length

	public static class GOSlimClient extends AbstractThreadedClient {
		public GOSlimClient(ExecutorServiceManager executorServiceManager) {
			super(executorServiceManager);
		}

		public HashMap<Protein, Set<GeneOntologyTerm>> searchProteinGOSlim(
				final Set<GeneOntologyTerm> GOSlimTerms,
				final Set<Protein> proteinSet
		) {
			return searchProteinGOSlim(GOSlimTerms, proteinSet, null);
		}

		/**
		 * Uses the "GAnnotation" service to retrieved a list of slimmed GO terms for a set of protein.
		 *
		 * @param GOSlimTerms a Set of GeneOntology term used as a GO slim
		 * @param proteinSet  a Set of Protein
		 */
		public HashMap<Protein, Set<GeneOntologyTerm>> searchProteinGOSlim(
				final Set<GeneOntologyTerm> GOSlimTerms,
				final Set<Protein> proteinSet,
				final ProgressMonitor progressMonitor
		) {
			final HashMap<Protein, Set<GeneOntologyTerm>> results = new HashMap<Protein, Set<GeneOntologyTerm>>();

			try {
				System.out.println("#2.1");
				final List<URI> URIs = generateRequests(
						new ArrayList<GeneOntologyTerm>(GOSlimTerms),
						new ArrayList<Protein>(proteinSet)
				);
				System.out.println(URIs);
				System.out.println("#2.2");
				final int[] i = new int[]{0};
				new ConcurrentExecutor<HashMap<Protein, Set<GeneOntologyTerm>>>(getExecutorServiceManager(), URIs.size()) {
					@Override
					public Callable<HashMap<Protein, Set<GeneOntologyTerm>>> submitRequests(final int index) {
						return new Callable<HashMap<Protein, Set<GeneOntologyTerm>>>() {
							@Override
							public HashMap<Protein, Set<GeneOntologyTerm>> call() throws Exception {
								final HashMap<Protein, Set<GeneOntologyTerm>> result =
										new HashMap<Protein, Set<GeneOntologyTerm>>();

								IOUtils.InputStreamConsumer consumer = new IOUtils.InputStreamConsumer() {
									@Override
									public void accept(InputStream inputStream) throws IOException {
										final InputStreamReader input = new InputStreamReader(inputStream);
										CSVReader reader = new CSVReader(input, '\t', '"', 1);

										String[] line;

										// Parse TSV response and create the HashMap output
										while ((line = reader.readNext()) != null) {
											if (line[0].isEmpty())
												break;

											final GeneOntologyTerm GOTerm = new GeneOntologyTerm(line[1], line[2], line[3].charAt(0));
											System.out.println(GOTerm);
											System.out.println("#2.3");

											for (Protein protein : proteinSet) {
												if (protein.getUniProtId().equals(line[0])) {
													Set<GeneOntologyTerm> ontologyTerms = result.get(protein);
													if (ontologyTerms == null) {
														ontologyTerms = new HashSet<GeneOntologyTerm>();
														result.put(protein, ontologyTerms);
													}
													ontologyTerms.add(GOTerm);
													break;
												}
											}
											System.out.println("#2.4");
										}
									}
								};

								// Launch HTTP request with retry
								IOUtils.fetchURLWithRetryAsInputStream(
										URIs.get(index).toString(),
										consumer,
										6000,
										4000,
										5,
										100
								);
								System.out.println(result);
								System.out.println("#2.5");
								return result;
							}
						};
					}

					@Override
					public void processResult(HashMap<Protein, Set<GeneOntologyTerm>> intermediaryResult, Integer index) {
						if(progressMonitor != null)
							progressMonitor.setProgress(((double)++i[0])/((double)URIs.size()));
						results.putAll(intermediaryResult);
					}
				}.run();

			} catch (URISyntaxException ignored) {}

			return results;
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

				if (estimatedLength > MAX_URL_LENGTH) {
					if (GOTermsStepLength > ProteinStepLength)
						numberOfGOTermsSubLists++;
					else if (ProteinStepLength > GOTermsStepLength)
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

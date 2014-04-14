package tk.nomis_tech.ppimapbuilder.data.client;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Abstract class definition of a protein ortholog search client. (With proposed implementation of threaded search).
 */
public abstract class ProteinOrthologClient {

	protected final int NB_THREAD = 3;

	/**
	 * Get the orthologous protein from the given organism
	 *
	 * @param protein  source protein
	 * @param organism desired organism
	 * @return orthologous protein
	 */
	public abstract Protein getOrtholog(Protein protein, Organism organism) throws IOException;

	/**
	 * Get the orthologous proteins from the given list of organisms.
	 *
	 * @param protein   source protein
	 * @param organisms list of desired organisms
	 * @return @code{Map} of ortholog proteins indexed by organism
	 */
	public Map<Organism, Protein> getOrthologsMultiOrganism(final Protein protein, final List<Organism> organisms) throws IOException {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrtholog()} for each given organism
		 */

		ExecutorService executorService = Executors.newFixedThreadPool(NB_THREAD);
		List<Future<Protein>> requests = new ArrayList<Future<Protein>>();
		CompletionService<Protein> completionService = new ExecutorCompletionService<Protein>(executorService);

		for (final Organism organism : organisms) {
			requests.add(completionService.submit(new Callable<Protein>() {
				@Override
				public Protein call() throws Exception {
					return getOrtholog(protein, organism);
				}
			}));
		}

		HashMap<Organism, Protein> orthologs = new HashMap<Organism, Protein>();
		Future<Protein> request;
		Protein ortholog;
		for (Iterator iterator = requests.iterator(); iterator.hasNext(); ) {
			try {
				request = completionService.take();
				ortholog = request.get();
				if (ortholog != null)
					orthologs.put(ortholog.getOrganism(), ortholog);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return orthologs;
	}

	/**
	 * Get the orthologous proteins of a list of proteins in a list of organisms
	 *
	 * @param proteins  source proteins
	 * @param organisms list of desired organisms
	 * @return @code{Map} of ortholog proteins indexed by organism indexed by source protein
	 */
	public Map<Protein, Map<Organism, Protein>> getOrthologsMultiOrganismMultiProtein(final List<Protein> proteins, final List<Organism> organisms) throws IOException {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrthologsMultiOrganism()} for each given source protein
		 */
		ExecutorService executorService = Executors.newFixedThreadPool(NB_THREAD);
		List<Future<Map<Organism, Protein>>> requests = new ArrayList<Future<Map<Organism, Protein>>>();
		CompletionService<Map<Organism, Protein>> completionService = new ExecutorCompletionService<Map<Organism, Protein>>(executorService);

		for (final Protein protein : proteins) {
			requests.add(completionService.submit(new Callable<Map<Organism, Protein>>() {
				@Override
				public Map<Organism, Protein> call() throws Exception {
					return getOrthologsMultiOrganism(protein, organisms);
				}
			}));
		}

		HashMap<Protein, Map<Organism, Protein>> orthologss = new HashMap<Protein, Map<Organism, Protein>>();
		Future<Map<Organism, Protein>> request;
		Map<Organism, Protein> orthologs;
		for (Iterator iterator = requests.iterator(); iterator.hasNext(); ) {
			try {
				request = completionService.take();
				orthologs = request.get();
				if (orthologs != null) {
					orthologss.put(proteins.get(requests.indexOf(request)), orthologs);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return orthologss;
	}
}

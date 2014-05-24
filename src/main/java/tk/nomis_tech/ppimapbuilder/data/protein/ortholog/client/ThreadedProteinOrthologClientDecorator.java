package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client;

import tk.nomis_tech.ppimapbuilder.data.client.AbstractThreadedClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Decorator for {@link ProteinOrthologClient} which brings new methods with threaded behavior in order
 * to search multiple orthologs in multiple organisms concurrently.
 */
public class ThreadedProteinOrthologClientDecorator<POC extends ProteinOrthologClient> extends AbstractThreadedClient implements ThreadedProteinOrthologClient {

	private final POC proteinOrthologClient;

	/**
	 * @param maxNumberThread The maximum number of thread usable by the ortholog client.
	 *                If set under 4, the thread count can actually exceed 4 in the @code{getOrthologsMultiOrganismMultiProtein()} method.
	 *                Otherwise, this limit will be respected
	 */
	public ThreadedProteinOrthologClientDecorator(POC proteinOrthologClient, int maxNumberThread) {
		super(maxNumberThread);
		this.proteinOrthologClient = proteinOrthologClient;
	}

	public ThreadedProteinOrthologClientDecorator(POC proteinOrthologClient) {
		this(proteinOrthologClient, 9);
	}

	@Override
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		return proteinOrthologClient.getOrtholog(protein, organism, score);
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		return proteinOrthologClient.getOrthologGroup(protein, organism);
	}

	/**
	 * Get the orthologous proteins from the given list of organisms.
	 *
	 * @param protein   source protein
	 * @param organisms list of desired organisms
	 * @param score     minimum score for desired ortholog
	 * @return @code{Map} of ortholog proteins indexed by organism
	 */
	@Override
	public Map<Organism, OrthologScoredProtein> getOrthologsMultiOrganism(final Protein protein, final List<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrtholog()} for each given organism
		 */
		List<Future<OrthologScoredProtein>> requests = new ArrayList<Future<OrthologScoredProtein>>();
		CompletionService<OrthologScoredProtein> completionService = new ExecutorCompletionService<OrthologScoredProtein>(newFixedThreadPool());

		for (final Organism organism : organisms) {
			requests.add(completionService.submit(new Callable<OrthologScoredProtein>() {
				@Override
				public OrthologScoredProtein call() throws Exception {
					return getOrtholog(protein, organism, score);
				}
			}));
		}

		HashMap<Organism, OrthologScoredProtein> orthologs = new HashMap<Organism, OrthologScoredProtein>();
		Future<OrthologScoredProtein> request;
		OrthologScoredProtein ortholog;
		for (int i = 0, requestsSize = requests.size(); i < requestsSize; i++) {
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
	 * @param score     minimum score for desired ortholog
	 * @return @code{Map} of ortholog proteins indexed by organism indexed by source protein
	 */
	@Override
	public Map<Protein, Map<Organism, OrthologScoredProtein>> getOrthologsMultiOrganismMultiProtein(final List<Protein> proteins, final List<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrthologsMultiOrganism()} for each given source protein
		 */

		//Temporally change the thread limit to be sure not to exceed it once
		int oriNThread = maxNumberThread;
		maxNumberThread = Math.max((int) Math.sqrt(maxNumberThread), 2);
		List<Future<Map<Organism, OrthologScoredProtein>>> requests = new ArrayList<Future<Map<Organism, OrthologScoredProtein>>>();
		CompletionService<Map<Organism, OrthologScoredProtein>> completionService = new ExecutorCompletionService<Map<Organism, OrthologScoredProtein>>(newFixedThreadPool());

		for (final Protein protein : proteins) {
			requests.add(completionService.submit(new Callable<Map<Organism, OrthologScoredProtein>>() {
				@Override
				public Map<Organism, OrthologScoredProtein> call() throws Exception {
					return getOrthologsMultiOrganism(protein, organisms, score);
				}
			}));
		}

		HashMap<Protein, Map<Organism, OrthologScoredProtein>> out = new HashMap<Protein, Map<Organism, OrthologScoredProtein>>();
		Future<Map<Organism, OrthologScoredProtein>> request;
		Map<Organism, OrthologScoredProtein> orthologs;
		for (int i = 0, requestsSize = requests.size(); i < requestsSize; i++) {
			try {
				request = completionService.take();
				orthologs = request.get();
				if (orthologs != null) {
					out.put(proteins.get(requests.indexOf(request)), orthologs);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		maxNumberThread = oriNThread;
		return out;
	}

}

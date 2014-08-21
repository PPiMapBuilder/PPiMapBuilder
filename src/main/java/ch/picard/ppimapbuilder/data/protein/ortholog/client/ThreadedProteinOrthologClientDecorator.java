package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.*;
import java.util.concurrent.*;

/**
 * Decorator for {@link ProteinOrthologClient} which brings new methods with threaded behavior in order
 * to search multiple orthologs in multiple organisms concurrently.
 */
public class ThreadedProteinOrthologClientDecorator<POC extends ProteinOrthologClient> extends AbstractThreadedClient implements ThreadedProteinOrthologClient {

	private final POC proteinOrthologClient;

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
	public Map<Organism, OrthologScoredProtein> getOrthologsMultiOrganism(final Protein protein, final Collection<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrtholog()} for each given organism
		 */
		List<Future<OrthologScoredProtein>> requests = new ArrayList<Future<OrthologScoredProtein>>();

		ExecutorService executorService = newThreadPool();
		CompletionService<OrthologScoredProtein> completionService = new ExecutorCompletionService<OrthologScoredProtein>(executorService);

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
		for (Future<OrthologScoredProtein> ignored : requests) {
			try {
				if (executorService.isShutdown())
					break;

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
	public Map<Protein, Map<Organism, OrthologScoredProtein>> getOrthologsMultiOrganismMultiProtein(final Collection<? extends Protein> proteins, final Collection<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrthologsMultiOrganism()} for each given source protein
		 */

		List<Protein> proteinList = new ArrayList<Protein>(proteins);

		//Temporally change the thread limit to be sure not to exceed it once
		List<Future<Map<Organism, OrthologScoredProtein>>> requests =
				new ArrayList<Future<Map<Organism, OrthologScoredProtein>>>();

		ExecutorService executorService = newThreadPool();
		
		CompletionService<Map<Organism, OrthologScoredProtein>> completionService =
				new ExecutorCompletionService<Map<Organism, OrthologScoredProtein>>(executorService);

		for (final Protein protein : proteinList) {
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
		for (Future<Map<Organism, OrthologScoredProtein>> ignored : requests) {
			try {
				if (executorService.isShutdown())
					break;
				
				request = completionService.take();
				orthologs = request.get();
				if (orthologs != null) {
					out.put(proteinList.get(requests.indexOf(request)), orthologs);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

}

package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.util.concurrency.ConcurrentExecutor;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;

import java.util.*;
import java.util.concurrent.*;

/**
 * Decorator for {@link ProteinOrthologClient} which brings new methods with threaded behavior in order
 * to search multiple orthologs in multiple organisms concurrently.
 */
public class ThreadedProteinOrthologClientDecorator extends AbstractThreadedClient implements ThreadedProteinOrthologClient {

	// Decorated protein ortholog client
	private final ProteinOrthologClient proteinOrthologClient;

	public ThreadedProteinOrthologClientDecorator(ProteinOrthologClient proteinOrthologClient, ExecutorServiceManager executorServiceManager) {
		super(executorServiceManager);
		this.proteinOrthologClient = proteinOrthologClient;
	}

	@Override
	public List<OrthologScoredProtein> getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
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
	public Map<Organism, List<OrthologScoredProtein>> getOrthologsMultiOrganism(final Protein protein, final Collection<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrtholog()} for each given organism
		 */
		final HashMap<Organism, List<OrthologScoredProtein>> orthologs = new HashMap<Organism, List<OrthologScoredProtein>>();
		final List<Organism> organismList = new ArrayList<Organism>(organisms);

		new ConcurrentExecutor<List<OrthologScoredProtein>>(getExecutorServiceManager(), organisms.size()) {
			@Override
			public Callable<List<OrthologScoredProtein>> submitRequests(final int index) {
				return new Callable<List<OrthologScoredProtein>>() {
					@Override
					public List<OrthologScoredProtein> call() throws Exception {
						Organism organism = organismList.get(index);
						return organism != null ? getOrtholog(protein, organism, score) : null;
					}
				};
			}

			@Override
			public void processResult(List<OrthologScoredProtein> result, Integer index) {
				orthologs.put(organismList.get(index), result);
			}
		}.run();

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
	public Map<Protein, Map<Organism, List<OrthologScoredProtein>>> getOrthologsMultiOrganismMultiProtein(final Collection<? extends Protein> proteins, final Collection<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrthologsMultiOrganism()} for each given source protein
		 */
		final List<Protein> proteinList = new ArrayList<Protein>(proteins);
		final HashMap<Protein, Map<Organism, List<OrthologScoredProtein>>> out = new HashMap<Protein, Map<Organism, List<OrthologScoredProtein>>>();

		new ConcurrentExecutor<Map<Organism, List<OrthologScoredProtein>>>(getExecutorServiceManager(), proteinList.size()) {
			@Override
			public Callable<Map<Organism, List<OrthologScoredProtein>>> submitRequests(final int index) {
				return new Callable<Map<Organism, List<OrthologScoredProtein>>>() {
					@Override
					public Map<Organism, List<OrthologScoredProtein>> call() throws Exception {
						Protein protein = proteinList.get(index);
						return protein != null ? getOrthologsMultiOrganism(protein, organisms, score) : null;
					}
				};
			}

			@Override
			public void processResult(Map<Organism, List<OrthologScoredProtein>> result, Integer index) {
				out.put(proteinList.get(index), result);
			}
		}.run();

		return out;
	}
}

package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.client.AbstractThreadedClient;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import ch.picard.ppimapbuilder.util.ConcurrentExecutor;

import java.util.*;
import java.util.concurrent.*;

/**
 * Decorator for {@link ProteinOrthologClient} which brings new methods with threaded behavior in order
 * to search multiple orthologs in multiple organisms concurrently.
 */
public class ThreadedProteinOrthologClientDecorator extends AbstractThreadedClient implements ThreadedProteinOrthologClient {

	private final ProteinOrthologClient proteinOrthologClient;
	private final ExecutorService Lvl1ExecutorService;
	private final ExecutorService Lvl2ExecutorService;

	public ThreadedProteinOrthologClientDecorator(ProteinOrthologClient proteinOrthologClient, int maxNumberThread) {
		super(maxNumberThread);
		this.proteinOrthologClient = proteinOrthologClient;
		this.Lvl1ExecutorService = newThreadPool(maxNumberThread * 2);
		this.Lvl2ExecutorService = newThreadPool();
	}

	public ThreadedProteinOrthologClientDecorator(ProteinOrthologClient proteinOrthologClient) {
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
		final HashMap<Organism, OrthologScoredProtein> orthologs = new HashMap<Organism, OrthologScoredProtein>();
		final List<Organism> organismList = new ArrayList<Organism>(organisms);

		new ConcurrentExecutor<OrthologScoredProtein>(Lvl1ExecutorService, organisms.size()) {
			@Override
			public Callable<OrthologScoredProtein> submitRequests(final int index) {
				return new Callable<OrthologScoredProtein>() {
					@Override
					public OrthologScoredProtein call() throws Exception {
						return getOrtholog(protein, organismList.get(index), score);
					}
				};
			}

			@Override
			public void processResult(OrthologScoredProtein result, Integer index) {
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
	public Map<Protein, Map<Organism, OrthologScoredProtein>> getOrthologsMultiOrganismMultiProtein(final Collection<? extends Protein> proteins, final Collection<Organism> organisms, final Double score) throws Exception {
		/**
		 * Proposed implementation with thread pool which requests @code{getOrthologsMultiOrganism()} for each given source protein
		 */
		final List<Protein> proteinList = new ArrayList<Protein>(proteins);
		final HashMap<Protein, Map<Organism, OrthologScoredProtein>> out = new HashMap<Protein, Map<Organism, OrthologScoredProtein>>();

		new ConcurrentExecutor<Map<Organism, OrthologScoredProtein>>(Lvl2ExecutorService, proteinList.size()) {
			@Override
			public Callable<Map<Organism, OrthologScoredProtein>> submitRequests(final int index) {
				return new Callable<Map<Organism, OrthologScoredProtein>>() {
					@Override
					public Map<Organism, OrthologScoredProtein> call() throws Exception {
						return getOrthologsMultiOrganism(proteinList.get(index), organisms, score);
					}
				};
			}

			@Override
			public void processResult(Map<Organism, OrthologScoredProtein> result, Integer index) {
				out.put(proteinList.get(index), result);
			}
		}.run();

		return out;
	}
}

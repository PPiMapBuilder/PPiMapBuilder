package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.web;

import tk.nomis_tech.ppimapbuilder.data.client.AbstractThreadedClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClientDecorator;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.ThreadedProteinOrthologClient;

import java.util.List;
import java.util.Map;

public class ThreadedInParanoidClient extends AbstractThreadedClient implements ThreadedProteinOrthologClient {

	private final ThreadedProteinOrthologClientDecorator decorator;
	private final InParanoidClient inParanoidClient;

	public ThreadedInParanoidClient(int maxNumberThread) {
		super(maxNumberThread);
		inParanoidClient = new InParanoidClient();
		decorator = new ThreadedProteinOrthologClientDecorator(inParanoidClient, maxNumberThread);
	}

	@Override
	public Map<Protein, Map<Organism, OrthologScoredProtein>> getOrthologsMultiOrganismMultiProtein(final List<Protein> proteins, final List<Organism> organisms, final Double score) throws Exception {
		boolean cacheEnabled = inParanoidClient.cacheEnabled();

		if (!cacheEnabled) inParanoidClient.enableCache(true);
		Map<Protein, Map<Organism, OrthologScoredProtein>> result = decorator.getOrthologsMultiOrganismMultiProtein(proteins, organisms, score);
		if (!cacheEnabled) inParanoidClient.enableCache(false);

		return result;
	}

	@Override
	public Map<Organism, OrthologScoredProtein> getOrthologsMultiOrganism(final Protein protein, final List<Organism> organisms, final Double score) throws Exception {
		boolean cacheEnabled = inParanoidClient.cacheEnabled();

		if (!cacheEnabled) inParanoidClient.enableCache(true);
		Map<Organism, OrthologScoredProtein> result = decorator.getOrthologsMultiOrganism(protein, organisms, score);
		if (!cacheEnabled) inParanoidClient.enableCache(false);

		return result;
	}

	@Override
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		return decorator.getOrtholog(protein, organism, score);
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		return decorator.getOrthologGroup(protein, organism);
	}
}

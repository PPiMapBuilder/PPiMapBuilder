package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProteinOrthologClient implements ProteinOrthologClient {

	@Override
	public List<OrthologScoredProtein> getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		OrthologGroup group = getOrthologGroup(protein, organism);

		if (group == null && !ProteinUtils.UniProtId.isStrict(protein.getUniProtId())) {
			String id = ProteinUtils.UniProtId.extractStrictUniProtId(protein.getUniProtId());

			group = getOrthologGroup(new Protein(id, protein.getOrganism()), organism);
		}

		if (group != null) {
			List<OrthologScoredProtein> ortholog = group.getBestOrthologsInOrganism(organism);
			OrthologScoredProtein originalProtein = group.find(protein);

			if (!ortholog.isEmpty() && ortholog.get(0).getScore() >= score
					&& originalProtein != null && originalProtein.getScore() >= score)
				return ortholog;
		}
		return new ArrayList<OrthologScoredProtein>();
	}

}


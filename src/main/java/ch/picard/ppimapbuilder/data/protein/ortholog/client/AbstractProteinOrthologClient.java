package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

public abstract class AbstractProteinOrthologClient implements ProteinOrthologClient {

	@Override
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		OrthologGroup group = getOrthologGroup(protein, organism);

		if (group == null && !ProteinUtils.UniProtId.isStrict(protein.getUniProtId())) {
			String id = ProteinUtils.UniProtId.extractStrictUniProtId(protein.getUniProtId());

			group = getOrthologGroup(new Protein(id, protein.getOrganism()), organism);
		}

		if (group != null) {
			OrthologScoredProtein ortholog = group.getBestOrthologInOrganism(organism);

			if (ortholog != null && ortholog.getScore() >= score) {
				if (protein instanceof UniProtEntry) {
					((UniProtEntry) protein).addOrtholog(ortholog);
				}
				return ortholog;
			}
		}
		return null;
	}

}


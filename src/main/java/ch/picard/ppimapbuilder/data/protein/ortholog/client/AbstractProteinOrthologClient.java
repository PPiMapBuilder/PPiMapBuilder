package ch.picard.ppimapbuilder.data.protein.ortholog.client;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

public abstract class AbstractProteinOrthologClient implements ProteinOrthologClient {

	@Override
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		OrthologGroup group = getOrthologGroup(protein, organism);

		if (group == null) {
			if (!protein.getUniProtId().contains("-"))
				return null;
			else {
				String id = protein.getUniProtId().split("-")[0];
				group = getOrthologGroup(new Protein(id, protein.getOrganism()), organism);

				if (group == null)
					return null;
			}
		}

		OrthologScoredProtein ortholog = group.getBestOrthologInOrganism(organism);

		if (ortholog == null)
			return null;

		if (ortholog.getScore() >= score) {
			if (protein instanceof UniProtEntry) {
				((UniProtEntry) protein).addOrtholog(ortholog);
			}
			return ortholog;
		} else
			return null;
	}

}


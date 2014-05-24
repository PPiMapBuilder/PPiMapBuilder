package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

public abstract class AbstractProteinOrthologClient implements ProteinOrthologClient {

	@Override
	public OrthologScoredProtein getOrtholog(Protein protein, Organism organism, Double score) throws Exception {
		OrthologGroup group =  getOrthologGroup(protein, organism);

		if(group == null)
			return null;

		OrthologScoredProtein ortholog = group.getBestOrthologInOrganism(organism);

		if(ortholog == null)
			return null;

		if(ortholog.getScore() >= score) {
			if (protein instanceof UniProtEntry) {
				((UniProtEntry) protein).addOrtholog(ortholog);
			}
			return ortholog;
		}
		else
			return null;
	}

}


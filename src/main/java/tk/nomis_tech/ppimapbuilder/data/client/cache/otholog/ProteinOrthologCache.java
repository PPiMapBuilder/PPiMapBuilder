package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.util.List;

public interface ProteinOrthologCache {

	public void addOrthologGroup(List<Pair<? extends Protein>> proteinPairs) throws Exception;

	public void addOrthologGroup(Protein proteinA, Protein proteinB) throws Exception;

	public Protein getOrtholog(Protein protein, Organism organism) throws Exception;
}

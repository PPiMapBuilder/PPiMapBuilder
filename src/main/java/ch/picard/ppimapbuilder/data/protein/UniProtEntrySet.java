package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class UniProtEntrySet extends HashSet<UniProtEntry> {
	
	private static final long serialVersionUID = 1L;

	public UniProtEntry find(String uniprotId) {
		for(UniProtEntry prot: this) {
			if(prot.getUniProtId().equals(uniprotId))
				return prot;
		}
		return null;
	}

	public UniProtEntry findWithAccessions(String uniprotId) {
		for(UniProtEntry prot: this) {
			if(prot.getAccessions().contains(uniprotId))
				return prot;
		}
		return null;
	}
	
	public Map<Protein, UniProtEntry> getInOrg(Organism organism) {
		Map<Protein, UniProtEntry> proteins = new HashMap<Protein, UniProtEntry>();
		if (!this.isEmpty()) {
			boolean inRefOrg = this.iterator().next().getOrganism().equals(organism);

			for (UniProtEntry proteinEntry : this) {
				if(inRefOrg) {
					proteins.put(proteinEntry, proteinEntry);
				} else {
					Protein protein = proteinEntry.getOrtholog(organism);

					if (protein != null)
						proteins.put(protein, proteinEntry);
				}
			}
		}
		return proteins;
	}
	
	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}
	
	@Override
	public boolean add(UniProtEntry e) {
		return !contains(e.getUniProtId()) && super.add(e);
	}

}

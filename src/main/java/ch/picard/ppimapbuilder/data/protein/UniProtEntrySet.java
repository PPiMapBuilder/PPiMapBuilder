package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.HashSet;
import java.util.Set;

public class UniProtEntrySet extends HashSet<UniProtEntry> {
	
	private static final long serialVersionUID = 1L;

	public UniProtEntry find(String uniprotId) {
		for(UniProtEntry prot: this) {
			if(prot.getUniProtId().equals(uniprotId))
				return prot;
		}
		return null;
	}
	
	public Set<String> getAllAsUniProtId() {
		Set<String> out = new HashSet<String>();
		for(UniProtEntry prot: this)
			out.add(prot.getUniProtId());
		return out;
	}

	public Set<Protein> getInOrg(Organism organism) {
		Set<Protein> proteins = new HashSet<Protein>();
		if (!this.isEmpty()) {
			boolean inRefOrg = this.iterator().next().getOrganism().equals(organism);

			if(inRefOrg)
				return new HashSet<Protein>(this);

			for (UniProtEntry proteinEntry : this) {
				Protein protein = proteinEntry.getOrtholog(organism);

				if (protein != null)
					proteins.add(protein);
			}
		}
		return proteins;
	}
	
	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}
	
	@Override
	public boolean add(UniProtEntry e) {
		if(!contains(e.getUniProtId()))
			return super.add(e);
		else
			return false;
	}

}

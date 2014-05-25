package ch.picard.ppimapbuilder.data.protein;

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

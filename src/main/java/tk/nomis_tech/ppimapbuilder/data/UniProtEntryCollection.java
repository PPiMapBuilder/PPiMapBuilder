package tk.nomis_tech.ppimapbuilder.data;

import java.util.HashSet;
import java.util.Set;

public class UniProtEntryCollection extends HashSet<UniProtEntry>{
	
	private static final long serialVersionUID = 1L;

	public UniProtEntry find(String uniprotId) {
		for(UniProtEntry prot: this) {
			if(prot.getUniprotId().equals(uniprotId))
				return prot;
		}
		return null;
	}
	
	public Set<String> getAllAsUniProtId() {
		Set<String> out = new HashSet<String>();
		for(UniProtEntry prot: this)
			out.add(prot.getUniprotId());
		return out;
	}
	
	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}
	
	@Override
	public boolean add(UniProtEntry e) {
		if(!contains(e.getUniprotId()))
			return super.add(e);
		else
			return false;
	}
}

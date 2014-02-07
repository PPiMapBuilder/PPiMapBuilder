package tk.nomis_tech.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class UniProtProteinCollection extends HashSet<UniProtProtein>{
	
	private static final long serialVersionUID = 1L;

	public UniProtProtein find(String uniprotId) {
		for(UniProtProtein prot: this) {
			if(prot.getUniprotId().equals(uniprotId))
				return prot;
		}
		return null;
	}
	
	public List<String> getAllAsUniProtId() {
		ArrayList<String> out = new ArrayList<String>();
		for(UniProtProtein prot: this)
			out.add(prot.getUniprotId());
		return out;
	}
	
	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}
	
		
	@Override
	public boolean add(UniProtProtein e) {
		if(!contains(e.getUniprotId()))
			return super.add(e);
		else
			return false;
	}
}

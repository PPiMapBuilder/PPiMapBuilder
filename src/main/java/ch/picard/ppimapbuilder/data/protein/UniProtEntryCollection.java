package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.io.Serializable;
import java.util.*;

public class UniProtEntryCollection implements Collection<UniProtEntry>, Serializable {

	private static final long serialVersionUID = 1L;
	private final List<UniProtEntry> uniProtEntries;
	private final Organism organism;

	public UniProtEntryCollection(Organism organism) {
		this.organism = organism;
		uniProtEntries = new ArrayList<UniProtEntry>();
	}

	public UniProtEntry find(String uniprotId) {
		for (UniProtEntry prot : uniProtEntries) {
			if (prot.getUniProtId().equals(uniprotId))
				return prot;
		}
		for (UniProtEntry prot : uniProtEntries) {
			if (prot.getAccessions().contains(uniprotId))
				return prot;
		}
		return null;
	}

	/**
	 * Returns Map of all UniProt identifiers and their originated UniProtEntry searching in the specified organism.
	 */
	public Map<String, UniProtEntry> identifiersInOrganism(Organism organism) {
		Map<String, UniProtEntry> proteins = new HashMap<String, UniProtEntry>();
		boolean inRefOrg = organism.equals(this.organism);
		for (UniProtEntry proteinEntry : uniProtEntries) {
			if (inRefOrg) {
				proteins.put(proteinEntry.getUniProtId(), proteinEntry);
				for (String accession : proteinEntry.getAccessions())
					proteins.put(accession, proteinEntry);
			} else {
				for (Protein protein : proteinEntry.getOrthologs(organism))
					proteins.put(protein.getUniProtId(), proteinEntry);
			}
		}
		return proteins;
	}

	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}

	@Override
	public boolean add(UniProtEntry uniProtEntry) {
		return uniProtEntry != null && this.uniProtEntries.add(ProteinUtils.correctEntryOrganism(uniProtEntry, this.organism));
	}

	@Override
	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries) {
		boolean ok = false;
		for (UniProtEntry uniProtEntry : uniProtEntries)
			ok = ok || add(uniProtEntry);
		return ok;
	}

	@Override
	public boolean contains(Object o) {
		return this.uniProtEntries.contains(o);
	}

	@Override
	public int size() {
		return uniProtEntries.size();
	}

	@Override
	public boolean isEmpty() {
		return uniProtEntries.isEmpty();
	}

	@Override
	public Iterator<UniProtEntry> iterator() {
		return uniProtEntries.iterator();
	}

	@Override
	public Object[] toArray() {
		return uniProtEntries.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return uniProtEntries.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return uniProtEntries.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.uniProtEntries.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return uniProtEntries.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return uniProtEntries.retainAll(c);
	}

	@Override
	public void clear() {
		this.uniProtEntries.clear();
	}

}

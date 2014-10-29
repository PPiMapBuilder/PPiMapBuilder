package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.io.Serializable;
import java.util.*;

public class UniProtEntrySet implements Set<UniProtEntry>, Serializable {

	private static final long serialVersionUID = 1L;
	private final List<UniProtEntry> uniProtEntries;
	private final Organism organism;

	public UniProtEntrySet(Organism organism) {
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

	public Map<Protein, UniProtEntry> findProteinInOrganismWithReferenceEntry(Organism organism) {
		Map<Protein, UniProtEntry> proteins = new HashMap<Protein, UniProtEntry>();
		for (UniProtEntry proteinEntry : uniProtEntries) {
			if (proteinEntry.getOrganism().equals(organism)) {
				proteins.put(proteinEntry, proteinEntry);
			} else {
				Protein protein = proteinEntry.getOrtholog(organism);

				if (protein != null)
					proteins.put(protein, proteinEntry);
			}
		}
		return proteins;
	}

	public void clear() {
		uniProtEntries.clear();
	}

	@Override
	public boolean add(UniProtEntry uniProtEntry) {
		UniProtEntry existingEntry = find(uniProtEntry.getUniProtId());

		//All new entry
		if (existingEntry == null)
			return add(uniProtEntry, false);

		// Existing entry => Add to duplicates
		this.uniProtEntries.remove(existingEntry);
		return add(
				new UniProtEntry
						.Builder(existingEntry, uniProtEntry)
						.build(),
				false
		);
	}


	@Override
	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries) {
		boolean atLeastOneAdditionMade = false;

		Map<UniProtEntry, List<UniProtEntry>> duplicates = new HashMap<UniProtEntry, List<UniProtEntry>>();

		for (UniProtEntry uniProtEntry : uniProtEntries) {
			UniProtEntry existingEntry = find(uniProtEntry.getUniProtId());

			// All new entry
			if (existingEntry == null) {
				atLeastOneAdditionMade = true;
				add(uniProtEntry, false);
			}
			// Existing entry => Add to duplicates
			else {
				List<UniProtEntry> entries = duplicates.get(existingEntry);
				if (entries == null)
					duplicates.put(existingEntry, entries = new ArrayList<UniProtEntry>());
				entries.add(uniProtEntry);
			}
		}

		// Merge duplicates and add them back to Set
		for (UniProtEntry existingEntry : duplicates.keySet()) {
			List<UniProtEntry> currentDuplicates = duplicates.get(existingEntry);
			this.uniProtEntries.remove(existingEntry);
			add(
					new UniProtEntry
							.Builder(existingEntry, currentDuplicates.toArray(new UniProtEntry[currentDuplicates.size()]))
							.build(),
					false
			);
			atLeastOneAdditionMade = true;
		}

		return atLeastOneAdditionMade;
	}

	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries, boolean checkExists) {
		if(checkExists) return addAll(uniProtEntries);
		else return this.uniProtEntries.addAll(uniProtEntries);
	}

	@Override
	public boolean contains(Object o) {
		return o instanceof UniProtEntry && contains((UniProtEntry) o);
	}

	public boolean contains(UniProtEntry entry) {
		if (contains(entry.getUniProtId()))
			return true;
		for (String accession : entry.getAccessions()) {
			if (contains(accession))
				return true;
		}
		return false;
	}

	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
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
		for (Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return uniProtEntries.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return uniProtEntries.retainAll(c);
	}

	/**
	 * @param entry
	 * @param checkExisting     Check exists before adding or not
	 */
	public boolean add(UniProtEntry entry, boolean checkExisting) {
		if (checkExisting) return add(entry);
		else return uniProtEntries.add(organism == null ? entry : ProteinUtils.correctEntryOrganism(entry, organism));
	}
}

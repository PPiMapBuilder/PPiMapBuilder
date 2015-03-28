package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;

import java.io.Serializable;
import java.util.*;

/**
 * A custom Set containing UniProtEntry without duplicate using the primary UniProt identifier as a unique identifier.
 * This Set also stores orthology associations and merges entries when you try to add an entry that already exist
 */
public class UniProtEntrySet implements Set<UniProtEntry>, Serializable {

	private static final long serialVersionUID = 1L;
	private final Organism organism;
	private final Set<UniProtEntry> uniProtEntries;
	private final Map<Organism, Map<String, UniProtEntry>> uniprotEntriesIndexed;

	public UniProtEntrySet(Organism organism) {
		this.organism = organism;
		uniProtEntries = new HashSet<UniProtEntry>();
		uniprotEntriesIndexed = new HashMap<Organism, Map<String, UniProtEntry>>();
		uniprotEntriesIndexed.put(organism, new HashMap<String, UniProtEntry>());
	}

	public UniProtEntry findByPrimaryAccession(String uniprotId) {
		final Map<String, UniProtEntry> entriesId = uniprotEntriesIndexed.get(organism);
		if (entriesId != null) {
			return entriesId.get(uniprotId);
		}
		return null;
	}

	private boolean addEntry(UniProtEntry uniProtEntry) {
		if (uniProtEntry != null) {
			UniProtEntry correctedEntry = ProteinUtils.correctEntryOrganism(uniProtEntry, this.organism);
			uniProtEntries.add(correctedEntry);
			uniprotEntriesIndexed.get(organism).put(uniProtEntry.getUniProtId(), uniProtEntry);

			String strictId = ProteinUtils.UniProtId.extractStrictUniProtId(uniProtEntry.getUniProtId());
			if(!strictId.equals(uniProtEntry.getUniProtId())) {
				uniprotEntriesIndexed.get(organism).put(strictId, uniProtEntry);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean add(UniProtEntry uniProtEntry) {
		UniProtEntry existingEntry = findByPrimaryAccession(uniProtEntry.getUniProtId());

		//All new entry
		if (existingEntry == null)
			return addEntry(uniProtEntry);

		// Existing entry => Add to duplicates
		remove(existingEntry);
		return addEntry(
				new UniProtEntry
						.Builder(existingEntry, uniProtEntry)
						.build()
		);
	}

	public void addAll(UniProtEntrySet uniProtEntrySet) {
		addAll((Collection<? extends UniProtEntry>) uniProtEntrySet);
		uniprotEntriesIndexed.putAll(uniProtEntrySet.uniprotEntriesIndexed);
	}

	@Override
	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries) {
		boolean atLeastOneAdditionMade = false;

		Map<UniProtEntry, List<UniProtEntry>> duplicates = new HashMap<UniProtEntry, List<UniProtEntry>>();

		for (UniProtEntry uniProtEntry : uniProtEntries) {
			UniProtEntry existingEntry = findByPrimaryAccession(uniProtEntry.getUniProtId());

			// All new entry
			if (existingEntry == null) {
				addEntry(uniProtEntry);
				atLeastOneAdditionMade = true;
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
			remove(existingEntry);
			addEntry(
					new UniProtEntry
							.Builder(existingEntry, duplicates.get(existingEntry))
							.build()
			);
			atLeastOneAdditionMade = true;
		}

		return atLeastOneAdditionMade;
	}

	public void addOrtholog(Protein entry, List<? extends Protein> orthologs) {
		UniProtEntry uniProtEntry = null;
		if (entry instanceof UniProtEntry) {
			uniProtEntry = (UniProtEntry) entry;
			add(uniProtEntry);
		} else
			uniProtEntry = findByPrimaryAccession(entry.getUniProtId());

		if (uniProtEntry == null)
			return;

		for (Protein ortholog : orthologs) {
			Map<String, UniProtEntry> entries = uniprotEntriesIndexed.get(ortholog.getOrganism());
			if (entries == null) {
				entries = new HashMap<String, UniProtEntry>();
				uniprotEntriesIndexed.put(ortholog.getOrganism(), entries);
			}
			entries.put(ortholog.getUniProtId(), uniProtEntry);

			String strictId = ProteinUtils.UniProtId.extractStrictUniProtId(ortholog.getUniProtId());
			if(!strictId.equals(ortholog.getUniProtId())) {
				entries.put(strictId, uniProtEntry);
			}
		}
	}

	public void addOrthologs(Map<Protein, Map<Organism, List<OrthologScoredProtein>>> orthologs) {
		for (Protein entry : orthologs.keySet()) {
			for (Organism organism : orthologs.get(entry).keySet()) {
				addOrtholog(entry, orthologs.get(entry).get(organism));
			}
		}
	}

	public Map<String, UniProtEntry> identifiersInOrganism(Organism organism) {
		return uniprotEntriesIndexed.get(organism);
	}

	public Set<String> identifiersInOrganism(Collection<UniProtEntry> entries, Organism organism) {
		Set<String> identifiers = new HashSet<String>();
		final Map<String, UniProtEntry> inOrgEntries = uniprotEntriesIndexed.get(organism);
		if(inOrgEntries != null) {
			for(Map.Entry<String, UniProtEntry> entry : inOrgEntries.entrySet()) {
				if(entries.contains(entry.getValue())) {
					identifiers.add(entry.getKey());
				}
			}
		}
		return identifiers;
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof UniProtEntry) {
			UniProtEntry entry = ((UniProtEntry) o);
			uniProtEntries.remove(entry);
			uniprotEntriesIndexed.get(organism).remove(entry.getUniProtId());
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c)
			if (!remove(o))
				return false;
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return uniProtEntries.retainAll(c);
	}

	@Override
	public void clear() {
		uniProtEntries.clear();
		uniprotEntriesIndexed.clear();
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
	public boolean contains(Object o) {
		if (o instanceof String)
			return contains((String) o);
		else return o instanceof UniProtEntry && contains((UniProtEntry) o);
	}

	public boolean contains(String uniProtId) {
		return findByPrimaryAccession(uniProtId) != null;
	}

	public boolean contains(UniProtEntry entry) {
		return uniProtEntries.contains(entry);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	public List<Protein> getOrthologs(UniProtEntry entry) {
		ArrayList<Protein> orthologs = new ArrayList<Protein>();
		for(Organism organism : uniprotEntriesIndexed.keySet()) {
			if(!organism.equals(this.organism)) {
				for(String id : uniprotEntriesIndexed.get(organism).keySet()) {
					if(uniprotEntriesIndexed.get(organism).get(id).equals(entry)) {
						orthologs.add(new Protein(id, organism));
					}
				}
			}
		}
		return orthologs;
	}
}

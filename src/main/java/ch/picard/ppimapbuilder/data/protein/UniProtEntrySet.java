package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;

import java.io.Serializable;
import java.util.*;

public class UniProtEntrySet extends UniProtEntryCollection implements Set<UniProtEntry>, Serializable {

	private static final long serialVersionUID = 1L;

	public UniProtEntrySet(Organism organism) {
		super(organism);
	}

	@Override
	public boolean add(UniProtEntry uniProtEntry) {
		UniProtEntry existingEntry = find(uniProtEntry.getUniProtId());

		//All new entry
		if (existingEntry == null)
			return super.add(uniProtEntry);

		// Existing entry => Add to duplicates
		super.remove(existingEntry);
		return super.add(
				new UniProtEntry
						.Builder(existingEntry, uniProtEntry)
						.build()
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
				super.add(uniProtEntry);
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
			super.remove(existingEntry);
			super.add(
					new UniProtEntry
							.Builder(existingEntry, currentDuplicates.toArray(new UniProtEntry[currentDuplicates.size()]))
							.build()
			);
			atLeastOneAdditionMade = true;
		}

		return atLeastOneAdditionMade;
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

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

}

/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologScoredProtein;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		uniProtEntries = Sets.newHashSet();
		uniprotEntriesIndexed = Maps.newHashMap();
		uniprotEntriesIndexed.put(organism, Maps.<String, UniProtEntry>newHashMap());
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
			String strictId = ProteinUtils.UniProtId.extractStrictUniProtId(uniProtEntry.getUniProtId());

			uniProtEntries.add(correctedEntry);

			final Map<String, UniProtEntry> inOrgUniProtEntry = uniprotEntriesIndexed.get(organism);
			Set<String> identifiers = Sets.newHashSet();
			identifiers.add(uniProtEntry.getUniProtId());
			identifiers.add(strictId);
			identifiers.addAll(uniProtEntry.getAccessions());
			for (String identifier : identifiers) {
				inOrgUniProtEntry.put(identifier, uniProtEntry);
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

	@Override
	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries) {
		boolean atLeastOneAdditionMade = false;

		Map<UniProtEntry, List<UniProtEntry>> duplicates = Maps.newHashMap();

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
					duplicates.put(existingEntry, entries = Lists.newArrayList());
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
				entries = Maps.newHashMap();
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

	public boolean hasOrganism(Organism organism) {
		return uniprotEntriesIndexed.containsKey(organism);
	}

	public Map<String, UniProtEntry> identifiersInOrganism(Organism organism) {
		return Maps.newHashMap(uniprotEntriesIndexed.get(organism));
	}

	public Set<String> identifiersInOrganism(Collection<UniProtEntry> entries, Organism organism) {
		Set<String> identifiers = Sets.newHashSet();
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
		List<Protein> orthologs = Lists.newArrayList();
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

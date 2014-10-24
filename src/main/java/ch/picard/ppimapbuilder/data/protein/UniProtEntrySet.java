package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.*;

public class UniProtEntrySet extends HashSet<UniProtEntry> {

	private static final long serialVersionUID = 1L;

	public UniProtEntry find(String uniprotId) {
		for (UniProtEntry prot : this) {
			if (prot.getUniProtId().equals(uniprotId))
				return prot;
		}
		return null;
	}

	public UniProtEntry findWithAccessions(String uniprotId) {
		for (UniProtEntry prot : this) {
			if (prot.getAccessions().contains(uniprotId))
				return prot;
		}
		return null;
	}

	public Map<Protein, UniProtEntry> getProteinInOrganismWithReferenceEntry(Organism organism) {
		Map<Protein, UniProtEntry> proteins = new HashMap<Protein, UniProtEntry>();
		for (UniProtEntry proteinEntry : this) {
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

	@Override
	public boolean addAll(Collection<? extends UniProtEntry> uniProtEntries) {
		boolean atLeastOneAdditionMade = false;
		List<Pair<UniProtEntry>> duplicates = new ArrayList<Pair<UniProtEntry>>();

		for (UniProtEntry uniProtEntry : uniProtEntries) {
			UniProtEntry existingEntry = findWithAccessions(uniProtEntry.getUniProtId());

			//All new entry
			if (existingEntry == null) {
				atLeastOneAdditionMade = true;
				add(uniProtEntry);
			}
			//Not entirely identical to existing entry => add to duplicates
			else if (!existingEntry.isIdentical(uniProtEntry)) {
				duplicates.add(new Pair<UniProtEntry>(existingEntry, uniProtEntry));
				atLeastOneAdditionMade = true;
			}
			//Else not added to Set
		}

		//merge duplicates and add them back to Set
		for (Pair<UniProtEntry> duplicate : duplicates) {
			remove(duplicate.getFirst());
			add(
					new UniProtEntry
							.Builder(duplicate.getFirst(), duplicate.getSecond())
							.build()
			);
		}

		return atLeastOneAdditionMade;
	}

	public boolean contains(String uniProtId) {
		return find(uniProtId) != null;
	}

	@Override
	public boolean add(UniProtEntry e) {
		return !contains(e.getUniProtId()) && super.add(e);
	}

}

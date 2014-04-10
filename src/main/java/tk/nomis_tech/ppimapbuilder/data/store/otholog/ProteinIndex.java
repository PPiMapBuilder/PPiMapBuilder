package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import org.apache.commons.collections.set.ListOrderedSet;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Protein proteinIndex for OrganismPairOrthologCache.
 * This index stores protein by organism and then by integer index (by order of insert).
 */
public class ProteinIndex implements Serializable {
	/**
	 * The protein index is based on a HashMap of ListOrderedSet of Protein.
	 * ListOrderedSet objects, as their name indicates, are hybrid of list (sequence with index) and set (no duplicates)
	 * which keeps insertion order.
	 */
	private final HashMap<Organism, ListOrderedSet> proteinIndex;

	public ProteinIndex(Organism organismA, Organism organismB) {
		this.proteinIndex = new HashMap<Organism, ListOrderedSet>();
		this.proteinIndex.put(organismA, new ListOrderedSet());
		this.proteinIndex.put(organismB, new ListOrderedSet());
	}

	public int indexOfProtein(Protein protein) {
		try {
			return proteinIndex.get(protein.getOrganism()).indexOf(protein.getUniProtId());
		} catch (NullPointerException e) {
			return -1;
		}
	}

	/**
	 * Gets the protein at the given index in the given organism
	 * @param index
	 * @param org
	 * @return the requested protein or null if doesn't exits
	 */
	public Protein getProtein(int index, Organism org) {
		try {
			return (Protein) proteinIndex.get(org).get(index);
		} catch (NullPointerException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Adds a protein in the proteinIndex of an organism
	 *
	 * @param protein
	 * @return the index of the newly inserted protein (even if the protein already existed); -1 if error (organism unknown)
	 */
	public int addProtein(Protein protein) {
		try {
			if (this.proteinIndex.get(protein.getOrganism()).add(protein))
				return this.proteinIndex.get(protein.getOrganism()).size() - 1;
			else
				return indexOfProtein(protein);
		} catch (NullPointerException e) {
			return -1;
		}
	}

	@Override
	public String toString() {
		return proteinIndex.toString();
	}
}
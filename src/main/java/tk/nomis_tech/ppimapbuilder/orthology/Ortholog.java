package tk.nomis_tech.ppimapbuilder.orthology;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Kevin Gravouil
 */
public class Ortholog {

	/**
	 * Uniprot ID of orthologous protein for a given protein and a given taxID
	 */
	private HashMap<UniprotId, HashMap<Integer, UniprotId>> orthologs;
	/**
	 * NCBI Tax ID to InParanoid ID
	 */
	private static HashMap<Integer, Integer> ID_MAP = new HashMap<Integer, Integer>() {
		{
			put(9031, 255); // Gallus gallus
			put(9606, 264); // Homo sapiens
			put(3702, 188); // Arabidopsis thaliana
			put(6239, 229); // Caenorhabditis elegans
			put(7227, 242); // Drosophila Melanogaster
			put(10090, 128); // Mus musculus
			put(4932, 208); // Saccharomyces cerevisiae
			put(4896, 173); // Schizosaccharomyces pombe
//				put(, ); // 
		}
	};

	/**
	 * Convert a NCBI taxonomic ID to a InParanoid organism ID.
	 * @param ncbiTaxId
	 * @return 
	 */
	public static Integer translateTaxID(Integer ncbiTaxId) {
		return Ortholog.ID_MAP.get(ncbiTaxId);
	}

	public Ortholog() {
		this.orthologs = new HashMap<UniprotId, HashMap<Integer, UniprotId>>();
	}

	public void addOrtholog(UniprotId referenceID, Integer orthologOrganism, UniprotId orthologId) {
		if (!this.orthologs.containsKey(referenceID)) {
			this.orthologs.put(referenceID, new HashMap<Integer, UniprotId>());
		}
		this.orthologs.get(referenceID).put(orthologOrganism, orthologId);
	}

	public UniprotId getOrthologs(UniprotId referenceId, Integer orthologTaxId) {
		return orthologs.get(referenceId).get(orthologTaxId);
	}

	public Set<Integer> getOrthologOrganisms(UniprotId uniprotId) {
		return orthologs.get(uniprotId).keySet();
	}

	public boolean isEmpty() {
		return orthologs.isEmpty();
	}

	public void clear() {
		orthologs.clear();
	}

	public Integer get(Integer ncbiTaxId) {
		return ID_MAP.get(ncbiTaxId);
	}

	public boolean containsKey(Integer ncbiTaxId) {
		return ID_MAP.containsKey(ncbiTaxId);
	}

	public Set<Integer> keySet() {
		return ID_MAP.keySet();
	}

}

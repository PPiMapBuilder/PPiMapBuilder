package tk.nomis_tech.ppimapbuilder.data.organism;

import java.util.Arrays;
import java.util.List;

/**
 * Class that keeps a register of organisms in PMB
 */
public class UserOrganismRepository {

	private static UserOrganismRepository _instance;
	private final List<Organism> listOrganism;

	public static UserOrganismRepository getInstance() {
		if (_instance == null)
			_instance = new UserOrganismRepository();
		return _instance;
	}

	private UserOrganismRepository() {
		// TODO : load from file
		listOrganism = Arrays.asList(
				new Organism("Homo sapiens", 9606),
				new Organism("Arabidopsis thaliana", 3702),
				new Organism("Caenorhabditis elegans", 6239),
				new Organism("Drosophila melanogaster", 7227),
				new Organism("Mus musculus", 10090),
				new Organism("Saccharomyces cerevisiae", 4932),
				new Organism("Schizosaccharomyces pombe", 4896),
				new Organism("Plasmodium falciparum", 5833),
				new Organism("Gallus gallus", 9031)
		);
	}

	public List<Organism> getOrganisms() {
		return listOrganism;
	}

	public Organism getOrganismByTaxId(int taxId) {
		for (Organism org : listOrganism)
			if (org.getTaxId() == taxId)
				return org;
		return null;
	}

	public Organism getOrganismBySimpleName(String simpleName) {
		for (Organism org : listOrganism)
			if (org.getSimpleScientificName().equals(simpleName.trim()))
				return org;
		return null;
	}
}

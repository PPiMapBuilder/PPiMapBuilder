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
				new Organism("Homo sapiens", "HUMAN", "Human", 9606),
				new Organism("Arabidopsis thaliana", "ARATH", "Mouse-ear cress", 3702),
				new Organism("Caenorhabditis elegans", "CAEEL", "", 6239),
				new Organism("Drosophila melanogaster", "DROME", "Fruit fly", 7227),
				new Organism("Mus musculus", "MOUSE", "Mouse", 10090),
				new Organism("Saccharomyces cerevisiae (strain ATCC 204508 / S288c)", "YEAST", "Baker's yeast", 559292),
				new Organism("Schizosaccharomyces pombe (strain 972 / ATCC 24843)", "SCHPO", "Fission yeast", 284812),
				new Organism("Plasmodium falciparum (isolate 3D7)", "PLAF7", "", 36329),
				new Organism("Gallus gallus", "CHICK", "Chicken", 9031)
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

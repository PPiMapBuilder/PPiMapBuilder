package tk.nomis_tech.ppimapbuilder.data.store;

import java.util.Arrays;
import java.util.List;

/**
 * Class that keeps a register of organisms in PMB
 */
public class OrganismRepository {

	private static OrganismRepository _instance;
	private final List<Organism> listOrganism;

	public static OrganismRepository getInstance() {
		if (_instance == null)
			_instance = new OrganismRepository();
		return _instance;
	}

	private OrganismRepository() {
		listOrganism = Arrays.asList(
				new Organism("Homo", "sapiens", 9606, 264),
				new Organism("Arabidopsis", "thaliana", 3702, 188),
				new Organism("Caenorhabditis", "elegans", 6239, 229),
				new Organism("Drosophila", "Melanogaster", 7227, 242),
				new Organism("Mus", "musculus", 10090, 128),
				new Organism("Saccharomyces", "cerevisiae", 4932, 208),
				new Organism("Schizosaccharomyces", "pombe", 4896, 173),
				new Organism("Gallus", "gallus", 9031, 255)
		);
	}

	public List<Organism> getListOrganism() {
		return listOrganism;
	}

	public Organism getOrganismByTaxId(int taxId) {
		for (Organism org : listOrganism)
			if (org.getTaxId() == taxId)
				return org;
		return null;
	}

	public Organism getOrganismByInParanoidOrgId(int inparanoidOrgId) {
		for (Organism org : listOrganism)
			if (org.getInparanoidOrgID() == inparanoidOrgId)
				return org;
		return null;
	}
}

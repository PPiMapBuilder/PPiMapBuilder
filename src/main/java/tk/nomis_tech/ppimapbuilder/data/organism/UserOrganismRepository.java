package tk.nomis_tech.ppimapbuilder.data.organism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

/**
 * Class that keeps a register of organisms in PMB
 */
public class UserOrganismRepository {

	private static UserOrganismRepository _instance;
	private static List<Organism> listOrganism;

	public static UserOrganismRepository getInstance() {
		if (_instance == null)
			_instance = new UserOrganismRepository();
		return _instance;
	}

	private UserOrganismRepository() {
		

		System.out.println("#4.9.1");
		
		// TODO : load from file
		listOrganism = (ArrayList<Organism>) PMBSettings.getInstance().getOrganismList();
		/*listOrganism = Arrays.asList(
				new Organism("Homo sapiens", 9606),
				new Organism("Arabidopsis thaliana", 3702),
				new Organism("Caenorhabditis elegans", 6239),
				new Organism("Drosophila melanogaster", 7227),
				new Organism("Mus musculus", 10090),
				new Organism("Saccharomyces cerevisiae", 4932),
				new Organism("Schizosaccharomyces pombe", 4896),
				new Organism("Plasmodium falciparum", 5833),
				new Organism("Gallus gallus", 9031)
		);*/
		

		System.out.println("#4.9.3");
	}
	
	public static void resetUserOrganismRepository() {
		listOrganism = (ArrayList<Organism>) PMBSettings.getInstance().getOrganismList();
	}
	
	public static ArrayList<Organism> getDefaultOrganismList() {
		ArrayList<Organism> organismList = new ArrayList<Organism>();
		organismList.add(new Organism("Homo sapiens", "HUMAN", "Human", 9606));
		organismList.add(new Organism("Arabidopsis thaliana", "ARATH", "Mouse-ear cress", 3702));
		organismList.add(new Organism("Caenorhabditis elegans", "CAEEL", "", 6239));
		organismList.add(new Organism("Drosophila melanogaster", "DROME", "Fruit fly", 7227));
		organismList.add(new Organism("Mus musculus", "MOUSE", "Mouse", 10090));
		organismList.add(new Organism("Saccharomyces cerevisiae (strain ATCC 204508 / S288c)", "YEAST", "Baker's yeast", 559292));
		organismList.add(new Organism("Schizosaccharomyces pombe (strain 972 / ATCC 24843)", "SCHPO", "Fission yeast", 284812));
		organismList.add(new Organism("Plasmodium falciparum (isolate 3D7)", "PLAF7", "", 36329));
		organismList.add(new Organism("Gallus gallus", "CHICK", "Chicken", 9031));
		return organismList;
	}

	public List<Organism> getOrganisms() {
		return listOrganism;
	}
	
	public void setOrganisms(ArrayList<Organism> listOrganism) {
		this.listOrganism = listOrganism;
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
	
	public void addOrganism(Organism o) {
		listOrganism.add(o);
	}
	
	public void addOrganism(String scientificName) {
		Organism orga = InParanoidOrganismRepository.getInstance().getOrganismByScientificName(scientificName);		
		if (orga != null) {
			listOrganism.add(orga);
		}
	}
}

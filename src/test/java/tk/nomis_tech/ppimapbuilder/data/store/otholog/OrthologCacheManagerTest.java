package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

import java.io.IOException;

public class OrthologCacheManagerTest {
	//Test organisms
	private static Organism mouse;
	private static Organism human;

	//Test proteins
	private static Protein P04040;
	private static Protein Q06141;
	private static Protein Q58A65;
	private static Protein P35230;
	private static Protein O60271;
	private static Protein P24270;

	@BeforeClass
	public static void init() {
		human = OrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = OrganismRepository.getInstance().getOrganismByTaxId(10090);

		P04040 = new Protein("P04040", human);
		Q06141 = new Protein("Q06141", human);
		O60271 = new Protein("O60271", human);
		P35230 = new Protein("P35230", mouse);
		Q58A65 = new Protein("Q58A65", mouse);
		P24270 = new Protein("P24270", mouse);
	}

	@Test
	public void test() throws IOException {
		OrthologCacheManager cache = PMBStore.getInstance().getOrthologCacheManager();

		Protein ortholog = cache.getOrtholog(P04040, mouse);
	}
}

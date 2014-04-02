package tk.nomis_tech.ppimapbuilder.data.store;

import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

public class OrthologCacheTest {


	@Test
	public void test() {
		OrthologCache cache = PMBStore.getOrthologCache();

		Protein sourceProt = new Protein("P04040", OrganismRepository.getInstance().getOrganismByTaxId(9606));
		Organism destOrg = OrganismRepository.getInstance().getOrganismByTaxId(10060);

		Protein destProt = cache.getOrtholog(sourceProt, destOrg);
	}
}

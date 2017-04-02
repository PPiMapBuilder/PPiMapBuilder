package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import junit.framework.Assert;
import org.junit.Test;

public class UniProtEntryTest {

	private static Organism human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);

	@Test
	public void testBuildFromProtein() {
		final Protein protein = new Protein("AAAAAA", human);

		final UniProtEntry entry =
				new UniProtEntry.Builder(protein)
					.build();

		Assert.assertEquals(protein.getUniProtId(), entry.getUniProtId());
		Assert.assertEquals(protein.getOrganism(), entry.getOrganism());
	}
}

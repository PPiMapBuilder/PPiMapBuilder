package ch.picard.ppimapbuilder.data.organism;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OrganismTest extends TestCase {

	/**
	 * Tests formatting of organism names
	 */
	@Test
	public void testFormat() {
		String expected;
		String actual;

		List<Organism> organisms = Arrays.asList(
				new Organism("homo sapiens", 9606),
				new Organism("HOMO SAPIENS", 9606)
		);
		for (Organism organism : organisms) {
			expected = "H.sapiens";
			actual = organism.getAbbrName();
			Assert.assertEquals(expected, actual);

			expected = "Homo";
			actual = organism.getGenus();
			Assert.assertEquals(expected, actual);

			expected = "sapiens";
			actual = organism.getSpecies();
			Assert.assertEquals(expected, actual);
		}

	}

	//@Ignore
	@Test
	public void testGenerateInparanoidOrganismList() throws IOException {
		InParanoidOrganismRepository instance = InParanoidOrganismRepository.getInstance();

		instance.generateInparanoidOrganisms();

		//Organism failing to be retrieved => 145481
	}
}

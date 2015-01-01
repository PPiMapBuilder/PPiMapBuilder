package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProteinUtilsTest {

	@Test
	public void testIsStrict() {
		List<String> strictUniProtIDs = Arrays.asList("Q62406", "P04040");
		for (String strictUniProtID : strictUniProtIDs) {
			Assert.assertTrue(ProteinUtils.UniProtId.isStrict(strictUniProtID));
		}

		List<String> notStrictUniProtIDs = Arrays.asList("Q62406-1", "Q62406-PRO_00202300", "XXXXXX", "qsd,");
		for (String notStrictUniProtID : notStrictUniProtIDs) {
			Assert.assertFalse(ProteinUtils.UniProtId.isStrict(notStrictUniProtID));
		}
	}

	@Test
	public void testIsValid() {
		List<String> validUniProtIDs = Arrays.asList("Q62406-1", "Q62406-PRO_00202300", "P04040");
		for (String validUniProtID : validUniProtIDs) {
			Assert.assertTrue(ProteinUtils.UniProtId.isValid(validUniProtID));
		}

		List<String> notValidUniProtIDs = Arrays.asList("sd:,;", "aaaaaa", "------");
		for (String notValidUniProtID : notValidUniProtIDs) {
			Assert.assertFalse(ProteinUtils.UniProtId.isValid(notValidUniProtID));
		}
	}

	@Test
	public void testExtractStrictUniProtID() {
		List<Pair<String>> beforeAfterExtract = new ArrayList<Pair<String>>();
		beforeAfterExtract.add(new Pair<String>("Q62406-PRO_00202300", "Q62406"));
		beforeAfterExtract.add(new Pair<String>("P04040-1", "P04040"));

		for (Pair<String> beforeAfter : beforeAfterExtract) {
			Assert.assertEquals(
					ProteinUtils.UniProtId.extractStrictUniProtId(beforeAfter.getFirst()),
					beforeAfter.getSecond()
			);
		}
	}
}
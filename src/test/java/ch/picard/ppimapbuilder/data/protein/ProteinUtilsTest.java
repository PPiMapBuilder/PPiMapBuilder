package ch.picard.ppimapbuilder.data.protein;

import org.junit.Assert;
import org.junit.Test;

public class ProteinUtilsTest {


	@Test
	public void testUniProtID() {
		Assert.assertTrue(ProteinUtils.UniProtId.isValid("Q62406-1"));
		Assert.assertTrue(ProteinUtils.UniProtId.isValid("P04040"));
	}
}
package ch.picard.ppimapbuilder.data;

import junit.framework.Assert;
import org.junit.Test;

public class PairTest {

	@Test
	public void testIdentity() {
		Pair<String> a = new Pair<String>("a", "b");
		Pair<String> b = new Pair<String>("a", "b");

		Assert.assertEquals(a, b);

		a = new Pair<String>(null, null);
		b = new Pair<String>(null, null);

		Assert.assertEquals(a, b);

		a = new Pair<String>("", null);
		b = new Pair<String>("", null);

		Assert.assertEquals(a, b);

		a = new Pair<String>(null, "");
		b = new Pair<String>(null, "");

		Assert.assertEquals(a, b);
	}

}
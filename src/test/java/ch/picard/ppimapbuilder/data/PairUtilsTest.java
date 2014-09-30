package ch.picard.ppimapbuilder.data;

import ch.picard.ppimapbuilder.data.organism.Organism;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

public class PairUtilsTest {

	@Test
	public void testCreateCombinationsWithoutRepetition() throws Exception {
		Organism human, mouse, rat, chicken;

		List<Organism> organisms = Arrays.asList(
				human = new Organism("Homo sapiens", 9606),
				mouse = new Organism("Mus musculus", 10090),
				rat = new Organism("Rattus norvegicus", 10116),
				chicken = new Organism("Gallus gallus", 9031)
		);

		Set<Pair<Organism>> excepted = new HashSet<Pair<Organism>>();
		excepted.add(new Pair<Organism>(human, mouse));
		excepted.add(new Pair<Organism>(human, rat));
		excepted.add(new Pair<Organism>(chicken, human));
		excepted.add(new Pair<Organism>(mouse, rat));
		excepted.add(new Pair<Organism>(chicken, rat));
		excepted.add(new Pair<Organism>(chicken, mouse));

		Set<Pair<Organism>> actual = PairUtils.createCombinations(new HashSet<Organism>(organisms), false, true);

		Assert.assertEquals(excepted, actual);
	}

	@Test
	public void testCreateCombinationsWithRepetitionWithOrder() throws Exception {
		String a = "a";
		String b = "b";
		String c = "c";
		String d = "d";

		List<String> letters = Arrays.asList(b, a, d, c);

		Set<Pair<String>> excepted = new HashSet<Pair<String>>();
		excepted.add(new Pair<String>(a, a));
		excepted.add(new Pair<String>(a, b));
		excepted.add(new Pair<String>(a, c));
		excepted.add(new Pair<String>(a, d));

		excepted.add(new Pair<String>(b, b));
		excepted.add(new Pair<String>(b, c));
		excepted.add(new Pair<String>(b, d));

		excepted.add(new Pair<String>(c, c));
		excepted.add(new Pair<String>(c, d));

		excepted.add(new Pair<String>(d, d));

		Set<Pair<String>> actual = PairUtils.createCombinations(new HashSet<String>(letters), true, true);

		Assert.assertEquals(excepted, actual);
	}

	@Test
	public void testCreateCombinationsWithRepetitionWithoutOrder() throws Exception {
		String a = "a";
		String b = "b";
		String c = "c";
		String d = "d";

		List<String> letters = Arrays.asList(b, a, d, c);

		Set<Pair<String>> excepted = new HashSet<Pair<String>>();
		excepted.add(new Pair<String>(b, b));
		excepted.add(new Pair<String>(b, a));
		excepted.add(new Pair<String>(b, d));
		excepted.add(new Pair<String>(b, c));

		excepted.add(new Pair<String>(a, a));
		excepted.add(new Pair<String>(a, d));
		excepted.add(new Pair<String>(a, c));

		excepted.add(new Pair<String>(d, d));
		excepted.add(new Pair<String>(d, c));

		excepted.add(new Pair<String>(c, c));

		Set<Pair<String>> actual = PairUtils.createCombinations(new LinkedHashSet<String>(letters), true, false);

		for(Pair<String> exceptedPair : excepted)
			Assert.assertTrue(actual.contains(exceptedPair));
	}
}
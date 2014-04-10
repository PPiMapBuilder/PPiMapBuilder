package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.OrganismRepository;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

public class ProteinIndexTest {

	static ProteinIndex indexEmpty;
	static ProteinIndex indexExample;
	static ProteinIndex indexBig;
	static int maxIndexBig;
	//Test organisms
	private static Organism mouse;
	private static Organism human;
	//Test proteins
	private static Protein Q9ESJ7;
	private static Protein Q78IQ7;
	private static Protein Q01815;
	private static Protein Q99KG5;
	private static Protein Q8R2G6;
	private static Protein P10144;
	private static Protein Q06141;
	private static Protein P25685;
	private static Protein P04040;
	private static Protein P08246;
	private static Protein O95273;
	private static Protein P31689;

	@BeforeClass
	public static void init() {
		human = OrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = OrganismRepository.getInstance().getOrganismByTaxId(10090);

		//Mouse proteins
		Q9ESJ7 = new Protein("Q9ESJ7", mouse);
		Q78IQ7 = new Protein("Q78IQ7", mouse);
		Q01815 = new Protein("Q01815", mouse);
		Q99KG5 = new Protein("Q99KG5", mouse);
		Q8R2G6 = new Protein("Q8R2G6", mouse);

		//Human proteins
		P10144 = new Protein("P10144", human);
		Q06141 = new Protein("Q06141", human);
		P25685 = new Protein("P25685", human);
		P04040 = new Protein("P04040", human);
		P08246 = new Protein("P08246", human);
		O95273 = new Protein("O95273", human);
		P31689 = new Protein("P31689", human);

		indexEmpty = new ProteinIndex(human, mouse);
		System.out.println("Empty index: " + indexEmpty);

		indexExample = new ProteinIndex(human, mouse) {{
			addProtein(Q9ESJ7);
			addProtein(Q78IQ7);
			addProtein(Q01815);
			addProtein(Q99KG5);
			addProtein(Q8R2G6);

			addProtein(P10144);
			addProtein(Q06141);
			addProtein(P25685);
			addProtein(P04040);
			addProtein(P08246);
			addProtein(O95273);
			addProtein(P31689);
		}};
		System.out.println("Example index: " + indexExample);

		indexBig = new ProteinIndex(human, mouse) {{
			int i = 0;
			for (; i <= 10000; i++) {
				addProtein(new Protein(Integer.toString(i), mouse));
				addProtein(new Protein(Integer.toString(i), human));
			}
			addProtein(Q01815);
			addProtein(P04040);
			maxIndexBig = i;
		}};
		//System.out.println("Empty index: "+indexBig);
	}

	@Test
	public void testIndexOfProteinFailWhenEmpty() throws Exception {
		int expected = -1;
		int actual;

		actual = indexEmpty.indexOfProtein(P04040);
		Assert.assertEquals(expected, actual);

		actual = indexEmpty.indexOfProtein(Q01815);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetProteinFailWhenEmpty() throws Exception {
		Protein actual;

		actual = indexEmpty.getProtein(0, human);
		Assert.assertNull(actual);

		actual = indexEmpty.getProtein(0, mouse);
		Assert.assertNull(actual);
	}

	@Test
	public void testIndexOfProteinSuccess() throws Exception {
		int expected = -1;
		int actual;

		{
			expected = 0;
			actual = indexExample.indexOfProtein(P10144);
			Assert.assertEquals(expected, actual);

			expected = 3;
			actual = indexExample.indexOfProtein(P04040);
			Assert.assertEquals(expected, actual);

			expected = 5;
			actual = indexExample.indexOfProtein(O95273);
			Assert.assertEquals(expected, actual);
		}
		{
			expected = 0;
			actual = indexExample.indexOfProtein(Q9ESJ7);
			Assert.assertEquals(expected, actual);

			expected = 3;
			actual = indexExample.indexOfProtein(Q99KG5);
			Assert.assertEquals(expected, actual);

			expected = 4;
			actual = indexExample.indexOfProtein(Q8R2G6);
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testGetProteinSuccess() throws Exception {
		Protein expected;
		Protein actual;

		{
			expected = P10144;
			actual = indexExample.getProtein(0, human);
			Assert.assertEquals(expected, actual);

			expected = P04040;
			actual = indexExample.getProtein(3, human);
			Assert.assertEquals(expected, actual);

			expected = O95273;
			actual = indexExample.getProtein(5, human);
			Assert.assertEquals(expected, actual);
		}

		{
			expected = Q9ESJ7;
			actual = indexExample.getProtein(0, mouse);
			Assert.assertEquals(expected, actual);

			expected = Q99KG5;
			actual = indexExample.getProtein(3, mouse);
			Assert.assertEquals(expected, actual);

			expected = Q8R2G6;
			actual = indexExample.getProtein(4, mouse);
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testGetProteinSpeedOnBigIndexHuman() throws Exception {
		Protein expected;
		Protein actual;

		expected = P04040;
		actual = indexBig.getProtein(maxIndexBig, human);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetProteinSpeedOnBigIndexMouse() throws Exception {
		Protein expected;
		Protein actual;

		expected = Q01815;
		actual = indexBig.getProtein(maxIndexBig, mouse);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testIndexOfProteinSpeedOnBigIndexHuman() throws Exception {
		int expected;
		int actual;

		expected = maxIndexBig;
		actual = indexBig.indexOfProtein(P04040);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testIndexOfProteinSpeedOnBigIndexMouse() throws Exception {
		int expected;
		int actual;

		expected = maxIndexBig;
		actual = indexBig.indexOfProtein(Q01815);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testAddProtein() throws Exception {
		int expected;
		int actual;

		final Protein P24270 = new Protein("P24270", mouse);
		final Protein Q9Y4A0 = new Protein("Q9Y4A0", mouse);

		//Insert and verify it has been inserted with the index returned
		expected = indexExample.addProtein(P24270);
		actual = indexExample.indexOfProtein(P24270);
		Assert.assertEquals(expected, actual);

		//Retest
		expected = indexExample.addProtein(Q9Y4A0);
		actual = indexExample.indexOfProtein(Q9Y4A0);
		Assert.assertEquals(expected, actual);

		//Check when we add a protein all ready existing => should just return the insertion index of the protein
		expected = indexExample.addProtein(P24270);
		actual = indexExample.indexOfProtein(P24270);
		Assert.assertEquals(expected, actual);
	}
}

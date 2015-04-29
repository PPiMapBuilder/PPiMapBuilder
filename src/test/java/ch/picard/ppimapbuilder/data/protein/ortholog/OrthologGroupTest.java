/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.data.protein.ortholog;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import ch.picard.ppimapbuilder.data.protein.Protein;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrthologGroupTest {

	private static Organism human;
	private static Organism mouse;

	//Mouse proteins
	private static Protein P24270;
	private static Protein D3Z748;
	private static Protein E9PXN7;
	private static Protein Q6ZQM8;

	//Human proteins
	private static Protein P04040;
	private static Protein Q9HAW7;
	private static Protein Q9HAW9;
	private static Protein Q9HAW8;
	private static Protein O60656;
	private static Protein Q62452;

	private static OrthologGroup catalaseOrthologGroup;
	private static OrthologGroup UDPOrthologGroup;
	private static OrthologGroup inValidGroup;

	@BeforeClass
	public static void before() throws IOException {

		human = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(9606);
		mouse = InParanoidOrganismRepository.getInstance().getOrganismByTaxId(10090);

		//Mouse proteins
		P24270 = new Protein("P24270", mouse);

		E9PXN7 = new Protein("E9PXN7", mouse);
		Q62452 = new Protein("Q62452", mouse);
		D3Z748 = new Protein("D3Z748", mouse);
		Q6ZQM8 = new Protein("Q6ZQM8", mouse);

		//Human proteins
		P04040 = new Protein("P04040", human);

		Q9HAW7 = new Protein("Q9HAW7", human);
		Q9HAW9 = new Protein("Q9HAW9", human);
		Q9HAW8 = new Protein("Q9HAW8", human);
		O60656 = new Protein("O60656", human);

		UDPOrthologGroup = new OrthologGroup(
				new OrthologScoredProtein(Q9HAW7, 1d),
				new OrthologScoredProtein(Q9HAW9, 0.692d),
				new OrthologScoredProtein(Q9HAW8, 0.678d),
				new OrthologScoredProtein(O60656, 0.626d),
				new OrthologScoredProtein(E9PXN7, 1d),
				new OrthologScoredProtein(Q62452, 0.609d),
				new OrthologScoredProtein(D3Z748, 0.319d),
				new OrthologScoredProtein(Q6ZQM8, 0.246d)
		);

		catalaseOrthologGroup = new OrthologGroup(
				new OrthologScoredProtein(P04040, 1d),
				new OrthologScoredProtein(P24270, 1d)
		);

		inValidGroup = new OrthologGroup(new OrthologScoredProtein(P04040, 1d));
	}

	@Test
	public void testGetBestOrthologsInOrganismMultiple() {
		Protein AAAAAA = new Protein("AAAAAA", human);
		Protein aaaaaa = new Protein("aaaaaa", mouse);
		Protein aaaaab = new Protein("aaaaab", mouse);

		List<? extends Protein> expected, actual;
		OrthologGroup orthologGroup = new OrthologGroup(
				new OrthologScoredProtein(AAAAAA, 1d),
				new OrthologScoredProtein(aaaaaa, 1d),
				new OrthologScoredProtein(aaaaab, 1d)
		);

		expected = Arrays.asList(AAAAAA);
		actual = orthologGroup.getBestOrthologsInOrganism(human);
		Assert.assertEquals(expected, actual);

		expected = Arrays.asList(aaaaaa, aaaaab);
		actual = orthologGroup.getBestOrthologsInOrganism(mouse);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetBestOrthologsInOrganism() {
		List<? extends Protein> expected, actual;

		expected = Arrays.asList(Q9HAW7);
		actual = UDPOrthologGroup.getBestOrthologsInOrganism(human);
		Assert.assertEquals(expected, actual);

		expected = Arrays.asList(E9PXN7);
		actual = UDPOrthologGroup.getBestOrthologsInOrganism(mouse);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetOrganisms() {
		Set<Organism> actual, expected;

		expected = new HashSet<Organism>(Arrays.asList(human, mouse));

		actual = new HashSet<Organism>(UDPOrthologGroup.getOrganisms());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetProteins() {
		Set<Protein> actual, expected;

		expected = new HashSet<Protein>(Arrays.asList(
				Q9HAW7, Q9HAW9, Q9HAW8, O60656, E9PXN7, Q62452, D3Z748, Q6ZQM8
		));
		actual = new HashSet<Protein>(UDPOrthologGroup.getProteins());
		Assert.assertEquals(expected, actual);

		expected = new HashSet<Protein>(Arrays.asList(P04040, P24270));
		actual = new HashSet<Protein>(catalaseOrthologGroup.getProteins());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testContainsProtein() {
		Assert.assertTrue(UDPOrthologGroup.contains(Q9HAW7));
		Assert.assertTrue(UDPOrthologGroup.contains(E9PXN7));

		Assert.assertFalse(UDPOrthologGroup.contains(P04040));
	}

	@Test
	public void testContainsOrganism() {
		Assert.assertTrue(UDPOrthologGroup.contains(human));
		Assert.assertTrue(UDPOrthologGroup.contains(mouse));
	}

	@Test
	public void testIsValid() {
		Assert.assertTrue(UDPOrthologGroup.isValid());
		Assert.assertFalse(inValidGroup.isValid());
	}
}
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
    
package ch.picard.ppimapbuilder.data.ontology;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class GeneOntologyTermSetTest {

	private GeneOntologyTermSet geneOntologyTermSet;
	private GeneOntologyTerm term1;
	private GeneOntologyTerm term2;
	private GeneOntologyTerm term3;
	private GeneOntologyTerm term4;
	private GeneOntologyTerm term5;
	private GeneOntologyTerm term6;
	private GeneOntologyTerm term7;

	@Before
	public void before() {
		geneOntologyTermSet = new GeneOntologyTermSet();
		geneOntologyTermSet.add(term1 = new GeneOntologyTerm("term1", "", GeneOntologyCategory.BIOLOGICAL_PROCESS));
		geneOntologyTermSet.add(term2 = new GeneOntologyTerm("term2", "", GeneOntologyCategory.BIOLOGICAL_PROCESS));
		geneOntologyTermSet.add(term3 = new GeneOntologyTerm("term3", "", GeneOntologyCategory.BIOLOGICAL_PROCESS));
		geneOntologyTermSet.add(term4 = new GeneOntologyTerm("term4", "", GeneOntologyCategory.BIOLOGICAL_PROCESS));
		geneOntologyTermSet.add(term5 = new GeneOntologyTerm("term5", "", GeneOntologyCategory.CELLULAR_COMPONENT));
		geneOntologyTermSet.add(term6 = new GeneOntologyTerm("term6", "", GeneOntologyCategory.CELLULAR_COMPONENT));
		geneOntologyTermSet.add(term7 = new GeneOntologyTerm("term7", "", GeneOntologyCategory.MOLECULAR_FUNCTION));
	}

	@Test
	public void testGetByCategory() throws Exception {
		GeneOntologyTermSet expected, actual;

		expected = new GeneOntologyTermSet(Arrays.asList(term1, term2, term3, term4));
		actual = geneOntologyTermSet.getByCategory(GeneOntologyCategory.BIOLOGICAL_PROCESS);
		Assert.assertEquals(expected, actual);

		expected = new GeneOntologyTermSet(Arrays.asList(term5, term6));
		actual = geneOntologyTermSet.getByCategory(GeneOntologyCategory.CELLULAR_COMPONENT);
		Assert.assertEquals(expected, actual);

		expected = new GeneOntologyTermSet(Arrays.asList(term7));
		actual = geneOntologyTermSet.getByCategory(GeneOntologyCategory.MOLECULAR_FUNCTION);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testAsStringList() throws Exception {
		Set<String> expected = new HashSet<String>(Arrays.asList("term2", "term3", "term1", "term6", "term7", "term4", "term5"));
		Set<String> actual = new HashSet<String>(geneOntologyTermSet.asStringList());
		Assert.assertEquals(expected, actual);
	}
}
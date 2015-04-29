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
import org.junit.Test;

public class OntologyTermTest {

	@Test
	public void testHashCode() {
		int expected = new OntologyTerm("GO:0032088").hashCode();
		int actual   = new OntologyTerm("GO:0032088").hashCode();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testEquals() {
		OntologyTerm expected = new OntologyTerm("GO:0032088");
		OntologyTerm actual   = new OntologyTerm("GO:0032088");

		Assert.assertEquals(expected, actual);
	}

}
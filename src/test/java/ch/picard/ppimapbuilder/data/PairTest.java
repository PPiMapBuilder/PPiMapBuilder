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
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
    
package ch.picard.ppimapbuilder.data.interaction.client.web.miql;

import org.junit.Assert;
import org.junit.Test;

public class MiQLParameterBuilderTest {

	@Test
	public void test1() {
		String expected = "test";
		String actual = (new MiQLParameterBuilder("test")).toString();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test2() {
		String expected = "identifier:test";
		String actual = (new MiQLParameterBuilder("identifier", "test")).toString();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test3() {
		String expected = "identifier:\"test test\"";
		String actual = (new MiQLParameterBuilder("identifier", "test test")).toString();

		Assert.assertEquals(expected, actual);
	}

}

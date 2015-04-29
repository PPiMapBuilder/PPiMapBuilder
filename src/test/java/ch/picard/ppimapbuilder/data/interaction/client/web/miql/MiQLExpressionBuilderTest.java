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

import java.util.Arrays;
import java.util.List;

public class MiQLExpressionBuilderTest {

	@Test
	public void test1() {
		String expected = "test";

		MiQLExpressionBuilder b = new MiQLExpressionBuilder();
		b.add("test");
		String actual = b.toString();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test2() {
		String expected = "(test OR test)";

		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.add("test");
		expr.addCondition(MiQLExpressionBuilder.Operator.OR, "test");
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void test2root() {
		String expected = "test OR test";

		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.add("test");
		expr.addCondition(MiQLExpressionBuilder.Operator.OR, "test");
		expr.setRoot(true);
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test3() {
		String expected = "(test OR \"te st\")";

		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.add("test");
		expr.addCondition(MiQLExpressionBuilder.Operator.OR, "te st");
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void test4() {
		String expected = "(test OR \"te st\" AND 3123)";

		List<String> elems = Arrays.asList("test", "te st");
		
		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.addAllCondition(MiQLExpressionBuilder.Operator.OR, elems);
		expr.addCondition(MiQLExpressionBuilder.Operator.AND, "3123");
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void test5() {
		String expected = "(test \"te st\")";

		List<String> elems = Arrays.asList("test", "te st");
		
		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.addAll(elems);
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}
}

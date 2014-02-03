package tk.nomis_tech.ppimapbuilder.util.miql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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

		List<String> elems = Arrays.asList(new String[] { "test", "te st" });
		
		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.addAllCondition(MiQLExpressionBuilder.Operator.OR, elems);
		expr.addCondition(MiQLExpressionBuilder.Operator.AND, "3123");
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void test5() {
		String expected = "(test \"te st\")";

		List<String> elems = Arrays.asList(new String[] { "test", "te st" });
		
		MiQLExpressionBuilder expr = new MiQLExpressionBuilder();
		expr.addAll(elems);
		String actual = expr.toString();

		Assert.assertEquals(expected, actual);
	}
}

package tk.nomis_tech.ppimapbuilder.util.miql;

import org.junit.Assert;
import org.junit.Test;

import tk.nomis_tech.ppimapbuilder.webservice.miql.MiQLParameterBuilder;

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

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
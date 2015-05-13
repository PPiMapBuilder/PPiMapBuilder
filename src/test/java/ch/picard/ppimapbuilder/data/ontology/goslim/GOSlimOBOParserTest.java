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
    
package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;

public class GOSlimOBOParserTest {

	@Test
	public void testParseOBOFile() throws Exception {
		GOSlim expected = new GOSlim("oboFileTest");
		expected.add(new GeneOntologyTerm("GO:0005975", "carbohydrate metabolic process", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006091", "generation of precursor metabolites and energy", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006260", "DNA replication", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006281", "DNA repair", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006355", "regulation of transcription, DNA-templated", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006399", "tRNA metabolic process", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006457", "protein folding", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006461", "protein complex assembly", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006486", "protein glycosylation", 'P'));
		expected.add(new GeneOntologyTerm("GO:0006914", "autophagy", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007005", "mitochondrion organization", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007010", "cytoskeleton organization", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007031", "peroxisome organization", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007033", "vacuole organization", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007034", "vacuolar transport", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007059", "chromosome segregation", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007155", "cell adhesion", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007163", "establishment or maintenance of cell polarity", 'P'));
		expected.add(new GeneOntologyTerm("GO:0007346", "regulation of mitotic cell cycle", 'P'));
		expected.add(new GeneOntologyTerm("GO:0008150", "biological_process", 'P'));
		expected.add(new GeneOntologyTerm("GO:0016071", "mRNA metabolic process", 'P'));

		InputStream oboFile = getClass().getResourceAsStream("oboFileTest.obo");
		GeneOntologyTermSet actual = GOSlimOBOParser.parseOBOFile(oboFile, "oboFileTest");

		Assert.assertEquals(expected, actual);
	}
}
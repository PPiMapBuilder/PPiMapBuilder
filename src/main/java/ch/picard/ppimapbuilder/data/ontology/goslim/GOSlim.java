package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;

/**
 * Representation of a GO slim by a Set of Gene Ontology terms and a name.
 */
public class GOSlim extends GeneOntologyTermSet {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT = "Default GO slim";

	private final String name;

	public GOSlim(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static GOSlim getDefaultGOslim() {
		GOSlim geneOntologyTerms = new GOSlim(DEFAULT);
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006366", "transcription from RNA polymerase II promoter", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0001932", "regulation of protein phosphorylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0048666", "neuron development", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006917", "induction of apoptosis", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016567", "protein ubiquitination", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006916", "anti-apoptosis", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0000075", "cell cycle checkpoint", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016570", "histone modification", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016569", "covalent chromatin modification", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006606", "protein import into nucleus", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0043161", "proteasomal ubiquitin-dependent protein catabolic process", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006397", "mRNA processing", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0000398", "nuclear mRNA splicing, via spliceosome", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0001525", "angiogenesis", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0055074", "calcium ion homeostasis", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006354", "transcription elongation, DNA-dependent", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0000077", "DNA damage checkpoint", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0030217", "T cell differentiation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0007088", "regulation of mitosis", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0030183", "B cell differentiation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006611", "protein export from nucleus", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0048741", "skeletal muscle fiber development", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006302", "double-strand break repair", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016571", "histone methylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016579", "protein deubiquitination", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006405", "RNA export from nucleus", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0009452", "RNA capping", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016925", "protein sumoylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016574", "histone ubiquitination", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016575", "histone deacetylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0016572", "histone phosphorylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006282", "regulation of DNA repair", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0055013", "cardiac muscle cell development", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0000060", "protein import into nucleus, translocation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0046323", "glucose import", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0007127", "meiosis I", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006306", "DNA methylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0043549", "regulation of kinase activity", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0043408", "regulation of MAPK cascade", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0019083", "viral transcription", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0007187", "G-protein signaling, coupled to cyclic nucleotide second messenger", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0002429", "immune response-activating cell surface receptor signaling pathway", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0010833", "telomere maintenance via telomere lengthening", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0055007", "cardiac muscle cell differentiation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006007", "glucose catabolic process", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006370", "mRNA capping", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006364", "rRNA processing", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006475", "internal protein amino acid acetylation", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0008286", "insulin receptor signaling pathway", 'P'));
		geneOntologyTerms.add(new GeneOntologyTerm("GO:0006633", "fatty acid biosynthetic process", 'P'));
		return geneOntologyTerms;
	}

}

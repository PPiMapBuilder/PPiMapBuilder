package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import com.eclipsesource.json.JsonObject;

import java.util.*;

public class UniProtEntry extends Protein {

	transient private final String geneName;
	transient private final List<String> synonymGeneNames = new ArrayList<String>();
	transient private final String proteinName;
	transient private final String ecNumber;
	transient private final boolean reviewed;
	transient private final Set<GeneOntologyTerm> cellularComponents = new HashSet<GeneOntologyTerm>();
	transient private final Set<GeneOntologyTerm> biologicalProcesses = new HashSet<GeneOntologyTerm>();
	transient private final Set<GeneOntologyTerm> molecularFunctions = new HashSet<GeneOntologyTerm>();
	transient private final HashMap<Organism, Protein> orthologs = new HashMap<Organism, Protein>();

	public UniProtEntry(String uniprotId, String geneName, String ecNumber, Organism organism, String proteinName, boolean reviewed) {
		super(uniprotId, organism);
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
	}

	public Protein getOrtholog(Organism organism) {
		return orthologs.get(organism);
	}
	
	public List<Protein> getOrthologs() {
		return new ArrayList<Protein>(orthologs.values());
	}
	
	public List<String> getOrthologsAsJSONList() {
		List<String> orthologs = new ArrayList<String>();
		for(Protein ortholog:  getOrthologs())
			orthologs.add(ortholog.toJSON());
		return orthologs;
	}
	
	public Protein addOrtholog(Protein prot) {
		return orthologs.put(prot.getOrganism(), prot);
	}

	public List<String> getCellularComponentsAsJSONList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.cellularComponents)
			list.add(go.toJSON());
		return list;
	}
	public List<String> getCellularComponentsAsSringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.cellularComponents)
			list.add(go.toString());
		return list;
	}

	public void addCellularComponent(GeneOntologyTerm go) {
		this.cellularComponents.add(go);
	}

	public List<String> getBiologicalProcessesAsJSONList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.biologicalProcesses)
			list.add(go.toJSON());
		return list;
	}
	public List<String> getBiologicalProcessesAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.biologicalProcesses)
			list.add(go.toString());
		return list;
	}

	public void addBiologicalProcess(GeneOntologyTerm go) {
		this.biologicalProcesses.add(go);
	}

	public List<String> getMolecularFunctionsAsJSONList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.molecularFunctions) {
			list.add(go.toJSON());
		}
		return list;
	}
	public List<String> getMolecularFunctionsAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.molecularFunctions) {
			list.add(go.toString());
		}
		return list;
	}

	public void addMolecularFunction(GeneOntologyTerm go) {
		this.molecularFunctions.add(go);
	}

	public List<String> getSynonymGeneNames() {
		return synonymGeneNames;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public String getGeneName() {
		return geneName;
	}

	public String getProteinName() {
		return proteinName;
	}

	public String getEcNumber() {
		return ecNumber;
	}

	public void addAllSynonymGeneNames(Collection<String> synonymGeneNames) {
		this.synonymGeneNames.addAll(synonymGeneNames);
	}

	/**
	 * Mostly for debug
	 */
	@Override
	public String toJSON() {
		JsonObject out = new JsonObject();
		out.add("id", uniProtId);
		try {
			out.add("taxId", getOrganism().getTaxId());
		} catch (NullPointerException e) {
			out.add("taxId", "null");
		}
		out.add("proteinName", proteinName);
		out.add("geneName", geneName);
		out.add("reviewed", reviewed);
		return out.toString();
	}
}

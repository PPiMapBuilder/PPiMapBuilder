package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import com.eclipsesource.json.JsonObject;
import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UniProtEntry extends Protein {

	transient private String geneName;
	transient private List<String> synonymGeneNames = new ArrayList<String>();
	transient private String proteinName;
	transient private String ecNumber;
	transient private boolean reviewed;
	transient private List<GeneOntologyTerm> cellularComponents = new ArrayList<GeneOntologyTerm>();
	transient private List<GeneOntologyTerm> biologicalProcesses = new ArrayList<GeneOntologyTerm>();
	transient private List<GeneOntologyTerm> molecularFunctions = new ArrayList<GeneOntologyTerm>();
	transient private final HashMap<Organism, Protein> orthologs = new HashMap<Organism, Protein>();

	public UniProtEntry(String uniprotId, String geneName, String ecNumber, Organism organism, String proteinName, boolean reviewed) {
		super(uniprotId, organism);
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
	}

	public List<GeneOntologyTerm> getCellularComponents() {
		return cellularComponents;
	}

	public ArrayList<String> getCellularComponentsAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.cellularComponents) {
			list.add(go.toString());
		}
		return list;
	}
	
	public Protein getOrtholog(Organism organism) {
		return orthologs.get(organism);
	}
	
	public List<Protein> getOrthologs() {
		return new ArrayList<Protein>(orthologs.values());
	}
	
	public List<String> getOrthologsAsStringList() {
		List<String> orthologs = new ArrayList<String>();
		for(Protein ortholog:  getOrthologs())
			orthologs.add(ortholog.toString());
		return orthologs;
	}
	
	public Protein addOrtholog(Protein prot) {
		return orthologs.put(prot.getOrganism(), prot);
	}

	public void setCellularComponents(ArrayList<GeneOntologyTerm> cellularComponents) {
		this.cellularComponents = cellularComponents;
	}

	public void addCellularComponent(GeneOntologyTerm go) {
		this.cellularComponents.add(go);
	}

	public List<GeneOntologyTerm> getBiologicalProcesses() {
		return biologicalProcesses;
	}

	public ArrayList<String> getBiologicalProcessesAsStringList() {

		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.biologicalProcesses) {
			list.add(go.toString());
		}
		return list;
	}

	public void setBiologicalProcesses(ArrayList<GeneOntologyTerm> biologicalProcesses) {
		this.biologicalProcesses = biologicalProcesses;
	}

	public void addBiologicalProcess(GeneOntologyTerm go) {
		this.biologicalProcesses.add(go);
	}

	public List<GeneOntologyTerm> getMolecularFunctions() {
		return molecularFunctions;
	}

	public ArrayList<String> getMolecularFunctionsAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.molecularFunctions) {
			list.add(go.toString());
		}
		return list;
	}

	public void setMolecularFunctions(ArrayList<GeneOntologyTerm> molecularFunctions) {
		this.molecularFunctions = molecularFunctions;
	}

	public void addMolecularFunction(GeneOntologyTerm go) {
		this.molecularFunctions.add(go);
	}

	public String molecularFunctionsToString() {
		String text = "";
		for (GeneOntologyTerm go : this.molecularFunctions) {
			text += go.toString();
			text += ",";
		}
		return text.substring(0, text.length() - 1);
	}

	public List<String> getSynonymGeneNames() {
		return synonymGeneNames;
	}

	public void setSynonymGeneNames(ArrayList<String> synonymGeneNames) {
		this.synonymGeneNames = synonymGeneNames;
	}

	public void addSynonymGeneName(String geneName) {
		this.synonymGeneNames.add(geneName);
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public String getProteinName() {
		return proteinName;
	}

	public void setProteinName(String proteinName) {
		this.proteinName = proteinName;
	}
	
	/**
	 * Mostly for debug
	 */
	@Override
	public String toString() {
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

	public String getEcNumber() {
		return ecNumber;
	}

	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
	}

}

package tk.nomis_tech.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UniProtEntry extends AbstractProtein {

	private String geneName;
	private ArrayList<String> synonymGeneNames = new ArrayList<String>();
	private String proteinName;
	private String ecNumber;
	private boolean reviewed;
	private ArrayList<GeneOntologyModel> cellularComponents = new ArrayList<GeneOntologyModel>();
	private ArrayList<GeneOntologyModel> biologicalProcesses = new ArrayList<GeneOntologyModel>();
	private ArrayList<GeneOntologyModel> molecularFunctions = new ArrayList<GeneOntologyModel>();
	private final HashMap<Integer, OrthologProtein> orthologs = new HashMap<Integer, OrthologProtein>();

	public UniProtEntry(String uniprotId, String geneName, String ecNumber, Integer taxId, String proteinName, boolean reviewed) {
		super(uniprotId, taxId);
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
	}

	public ArrayList<GeneOntologyModel> getCellularComponents() {
		return cellularComponents;
	}

	public ArrayList<String> getCellularComponentsAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyModel go : this.cellularComponents) {
			list.add(go.toString());
		}
		return list;
	}
	
	public OrthologProtein getOrthologByTaxid(Integer taxid) {
		return orthologs.get(taxid);
	}
	
	public List<OrthologProtein> getAllOrthologs() {
		return new ArrayList<OrthologProtein>(orthologs.values());
	}
	
	public OrthologProtein addOrtholog(OrthologProtein prot) {
		return orthologs.put(prot.getTaxId(), prot);
	}

	public void setCellularComponents(ArrayList<GeneOntologyModel> cellularComponents) {
		this.cellularComponents = cellularComponents;
	}

	public void addCellularComponent(GeneOntologyModel go) {
		this.cellularComponents.add(go);
	}

	public ArrayList<GeneOntologyModel> getBiologicalProcesses() {
		return biologicalProcesses;
	}

	public ArrayList<String> getBiologicalProcessesAsStringList() {

		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyModel go : this.biologicalProcesses) {
			list.add(go.toString());
		}
		return list;
	}

	public void setBiologicalProcesses(ArrayList<GeneOntologyModel> biologicalProcesses) {
		this.biologicalProcesses = biologicalProcesses;
	}

	public void addBiologicalProcess(GeneOntologyModel go) {
		this.biologicalProcesses.add(go);
	}

	public ArrayList<GeneOntologyModel> getMolecularFunctions() {
		return molecularFunctions;
	}

	public ArrayList<String> getMolecularFunctionsAsStringList() {

		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyModel go : this.molecularFunctions) {
			list.add(go.toString());
		}
		return list;
	}

	public void setMolecularFunctions(ArrayList<GeneOntologyModel> molecularFunctions) {
		this.molecularFunctions = molecularFunctions;
	}

	public void addMolecularFunction(GeneOntologyModel go) {
		this.molecularFunctions.add(go);
	}

	public String molecularFunctionsToString() {
		String text = "";
		for (GeneOntologyModel go : this.molecularFunctions) {
			text += go.toString();
			text += ",";
		}
		return text.substring(0, text.length() - 1);
	}

	public ArrayList<String> getSynonymGeneNames() {
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
		StringBuilder out = new StringBuilder();
		out.append("{");
		out.append("id:").append(uniprotId).append(", ");
		out.append("taxId:").append(taxId).append(", ");
		out.append("proteinName:").append(proteinName).append(", ");
		out.append("geneName:").append(geneName).append(", ");
		out.append("reviewed:").append(reviewed);
		out.append("}");
		return out.toString();
	}

	public String getEcNumber() {
		return ecNumber;
	}

	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
	}

}

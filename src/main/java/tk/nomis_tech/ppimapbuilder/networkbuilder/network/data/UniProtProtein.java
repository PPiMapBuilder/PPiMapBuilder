package tk.nomis_tech.ppimapbuilder.networkbuilder.network.data;

import java.util.ArrayList;

public class UniProtProtein extends AbstractProtein {

	private String geneName;
	private ArrayList<String> synonymGeneNames = new ArrayList<String>();
	private String proteinName;
	private String ecNumber;
	public String getEcNumber() {
		return ecNumber;
	}

	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
	}

	private boolean reviewed;
	private ArrayList<GeneOntologyModel> cellularComponents = new ArrayList<GeneOntologyModel>();
	private ArrayList<GeneOntologyModel> biologicalProcesses = new ArrayList<GeneOntologyModel>();
	private ArrayList<GeneOntologyModel> molecularFunctions = new ArrayList<GeneOntologyModel>();
	
	public UniProtProtein(String uniprotId, String geneName, String ecNumber, Integer taxId, String proteinName, boolean reviewed) {
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

	public void setCellularComponents(
			ArrayList<GeneOntologyModel> cellularComponents) {
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

	public void setBiologicalProcesses(
			ArrayList<GeneOntologyModel> biologicalProcesses) {
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

	public void setMolecularFunctions(
			ArrayList<GeneOntologyModel> molecularFunctions) {
		this.molecularFunctions = molecularFunctions;
	}
	
	public void addMolecularFunction(GeneOntologyModel go) {
		this.molecularFunctions.add(go);
	}
	
	public String molecularFunctionsToString() {
		String text = "";
		for (GeneOntologyModel go : this.molecularFunctions) {
			text+=go.toString();
			text+=",";
		}
		return text.substring(0, text.length()-1);
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
	
	


}

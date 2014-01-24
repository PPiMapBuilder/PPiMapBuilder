package tk.nomis_tech.ppimapbuilder.networkbuilder.network.data;

import java.util.ArrayList;

public class UniProtProtein extends AbstractProtein {

	private String geneName;
	private ArrayList<String> synonymGeneNames = new ArrayList<String>();
	private String proteinName;
	private boolean reviewed;
	
	public UniProtProtein(String uniprotId, String geneName, Integer taxId, String proteinName, boolean reviewed) {
		super(uniprotId, taxId);
		this.proteinName = proteinName;
		this.geneName = geneName;
		this.reviewed = reviewed;
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

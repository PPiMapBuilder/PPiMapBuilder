package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class UniProtEntry extends Protein {

	transient private final String geneName;
	transient private final List<String> synonymGeneNames;
	transient private final String proteinName;
	transient private final String ecNumber;
	transient private final boolean reviewed;
	transient private final Set<GeneOntologyTerm> cellularComponents;
	transient private final Set<GeneOntologyTerm> biologicalProcesses;
	transient private final Set<GeneOntologyTerm> molecularFunctions;
	transient private final HashMap<Organism, Protein> orthologs = new HashMap<Organism, Protein>();

	public UniProtEntry(
			String uniprotId,
			String geneName,
			String ecNumber,
			Organism organism,
			String proteinName,
			boolean reviewed,
			List<String> synonymGeneNames,
			Set<GeneOntologyTerm> biologicalProcesses,
			Set<GeneOntologyTerm> cellularComponents,
			Set<GeneOntologyTerm> molecularFunctions
	) {
		super(uniprotId, organism);
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
		this.synonymGeneNames = synonymGeneNames;
		this.cellularComponents = cellularComponents;
		this.biologicalProcesses = biologicalProcesses;
		this.molecularFunctions = molecularFunctions;
	}

	public Protein getOrtholog(Organism organism) {
		return orthologs.get(organism);
	}

	public List<Protein> getOrthologs() {
		return new ArrayList<Protein>(orthologs.values());
	}

	public Protein addOrtholog(Protein prot) {
		return orthologs.put(prot.getOrganism(), prot);
	}

	public Set<GeneOntologyTerm> getCellularComponents() {
		return cellularComponents;
	}

	public Set<GeneOntologyTerm> getBiologicalProcesses() {
		return biologicalProcesses;
	}

	public Set<GeneOntologyTerm> getMolecularFunctions() {
		return molecularFunctions;
	}

	public List<String> getCellularComponentsAsSringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.cellularComponents)
			list.add(go.toString());
		return list;
	}

	public List<String> getBiologicalProcessesAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.biologicalProcesses)
			list.add(go.toString());
		return list;
	}

	public List<String> getMolecularFunctionsAsStringList() {
		ArrayList<String> list = new ArrayList<String>();
		for (GeneOntologyTerm go : this.molecularFunctions) {
			list.add(go.toString());
		}
		return list;
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

	public static class Builder {

		final UniProtEntry template;

		String uniprotId = null;
		Organism organism = null;
		String geneName = null;
		List<String> synonymGeneNames = null;
		String proteinName = null;
		String ecNumber = null;
		Boolean reviewed = null;
		Set<GeneOntologyTerm> cellularComponents = null;
		Set<GeneOntologyTerm> biologicalProcesses = null;
		Set<GeneOntologyTerm> molecularFunctions = null;
		HashMap<Organism, Protein> orthologs = null;

		public Builder(UniProtEntry entry) {
			this.template = entry;
		}

		public Builder setUniprotId(String uniprotId) {
			this.uniprotId = uniprotId;
			return this;
		}

		public Builder setOrganism(Organism organism) {
			this.organism = organism;
			return this;
		}

		public Builder setGeneName(String geneName) {
			this.geneName = geneName;
			return this;
		}

		public Builder setSynonymGeneNames(List<String> synonymGeneNames) {
			this.synonymGeneNames = synonymGeneNames;
			return this;
		}

		public Builder setProteinName(String proteinName) {
			this.proteinName = proteinName;
			return this;
		}

		public Builder setEcNumber(String ecNumber) {
			this.ecNumber = ecNumber;
			return this;
		}

		public Builder setReviewed(boolean reviewed) {
			this.reviewed = reviewed;
			return this;
		}

		public Builder setCellularComponents(Set<GeneOntologyTerm> cellularComponents) {
			this.cellularComponents = cellularComponents;
			return this;
		}

		public Builder setBiologicalProcesses(Set<GeneOntologyTerm> biologicalProcesses) {
			this.biologicalProcesses = biologicalProcesses;
			return this;
		}

		public Builder setMolecularFunctions(Set<GeneOntologyTerm> molecularFunctions) {
			this.molecularFunctions = molecularFunctions;
			return this;
		}

		public Builder setOrthologs(HashMap<Organism, Protein> orthologs) {
			this.orthologs = orthologs;
			return this;
		}

		public UniProtEntry build() {
			if (template != null) {
				if (uniprotId == null) uniprotId = template.getUniProtId();
				if (organism == null) organism = template.getOrganism();
				if (geneName == null) geneName = template.geneName;
				if (synonymGeneNames == null) synonymGeneNames = template.synonymGeneNames;
				if (proteinName == null) proteinName = template.proteinName;
				if (ecNumber == null) ecNumber = template.ecNumber;
				if (reviewed == null) reviewed = template.reviewed;
				if (orthologs == null) orthologs = template.orthologs;
				if (biologicalProcesses == null) biologicalProcesses = template.biologicalProcesses;
				if (cellularComponents == null) cellularComponents = template.cellularComponents;
				if (molecularFunctions == null) molecularFunctions = template.molecularFunctions;
			}
			UniProtEntry uniProtEntry = new UniProtEntry(
					uniprotId,
					geneName,
					ecNumber,
					organism,
					proteinName,
					reviewed,
					synonymGeneNames,
					biologicalProcesses,
					cellularComponents,
					molecularFunctions
			);
			for (Protein ortholog : orthologs.values()) {
				uniProtEntry.addOrtholog(ortholog);
			}
			return uniProtEntry;
		}

	}
}

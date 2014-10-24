package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import com.eclipsesource.json.JsonObject;

import java.util.*;

public class UniProtEntry extends Protein {

	transient private final LinkedHashSet<String> accessions;
	transient private final String geneName;
	transient private final Set<String> synonymGeneNames;
	transient private final String proteinName;
	transient private final String ecNumber;
	transient private final boolean reviewed;
	transient private final Set<GeneOntologyTerm> cellularComponents;
	transient private final Set<GeneOntologyTerm> biologicalProcesses;
	transient private final Set<GeneOntologyTerm> molecularFunctions;
	transient private final HashMap<Organism, Protein> orthologs = new HashMap<Organism, Protein>();

	public UniProtEntry(
			String uniprotId,
			LinkedHashSet<String> accessions,
			String geneName,
			String ecNumber,
			Organism organism,
			String proteinName,
			boolean reviewed,
			Set<String> synonymGeneNames,
			Set<GeneOntologyTerm> biologicalProcesses,
			Set<GeneOntologyTerm> cellularComponents,
			Set<GeneOntologyTerm> molecularFunctions
	) {
		super(uniprotId, organism);
		this.accessions = accessions;
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

	public Collection<Protein> getOrthologs() {
		return orthologs.values();
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

	public Set<String> getSynonymGeneNames() {
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

	public boolean isIdentical(UniProtEntry entry) {
		return uniProtId.equals(entry.uniProtId)
				&& organism.equals(entry.organism)
				&& accessions.equals(entry.accessions)
				&& orthologs.equals(entry.orthologs)
				&& synonymGeneNames.equals(entry.synonymGeneNames)
				&& biologicalProcesses.equals(entry.biologicalProcesses)
				&& cellularComponents.equals(entry.cellularComponents)
				&& molecularFunctions.equals(entry.molecularFunctions)
				&& geneName.equals(entry.geneName)
				&& ecNumber.equals(entry.ecNumber)
				&& proteinName.equals(entry.proteinName)
				&& reviewed == entry.reviewed;
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

	public LinkedHashSet<String> getAccessions() {
		return accessions;
	}

	/**
	 * UniProtEntry.Builder used to create new UniProt entry from a template which can be modified.
	 * Can also be used to merge several entry into one by taking the first one as a template and merging accessions,
	 * gene name synonymes, cellular component annotations, biological processes annotations, molecular function annotations
	 * and orthologs.
	 */
	public static class Builder {

		private final UniProtEntry template;

		private String uniprotId = null;
		private LinkedHashSet<String> accessions = null;
		private Organism organism = null;
		private String geneName = null;
		private Set<String> synonymGeneNames = null;
		private String proteinName = null;
		private String ecNumber = null;
		private Boolean reviewed = null;
		private Set<GeneOntologyTerm> cellularComponents = null;
		private Set<GeneOntologyTerm> biologicalProcesses = null;
		private Set<GeneOntologyTerm> molecularFunctions = null;
		private HashMap<Organism, Protein> orthologs = null;

		public Builder() {
			this((UniProtEntry) null);
		}

		public Builder(UniProtEntry entry) {
			this.template = entry;
		}

		public Builder(UniProtEntry... entries) {
			boolean first = true;
			for (UniProtEntry entry : entries) {
				if (first) {
					first = false;
					accessions = new LinkedHashSet<String>(entry.accessions);
					synonymGeneNames = new HashSet<String>(entry.synonymGeneNames);
					cellularComponents = new HashSet<GeneOntologyTerm>(entry.cellularComponents);
					biologicalProcesses = new HashSet<GeneOntologyTerm>(entry.biologicalProcesses);
					molecularFunctions = new HashSet<GeneOntologyTerm>(entry.molecularFunctions);
					orthologs = new HashMap<Organism, Protein>(entry.orthologs);
				} else {
					accessions.addAll(entry.accessions);
					accessions.add(entry.uniProtId);
					synonymGeneNames.addAll(entry.synonymGeneNames);
					cellularComponents.addAll(entry.cellularComponents);
					biologicalProcesses.addAll(entry.biologicalProcesses);
					molecularFunctions.addAll(entry.molecularFunctions);
					orthologs.putAll(entry.orthologs);
				}
			}

			this.template = entries[0];
		}

		public Builder setUniprotId(String uniprotId) {
			this.uniprotId = uniprotId;
			return this;
		}

		public Builder setAccessions(LinkedHashSet<String> accessions) {
			this.accessions = accessions;
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

		public Builder setSynonymGeneNames(Set<String> synonymGeneNames) {
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
			UniProtEntry uniProtEntry = new UniProtEntry(
				template != null && uniprotId == null           ? template.uniProtId           : uniprotId,
				template != null && accessions == null          ? template.accessions          : accessions,
				template != null && geneName == null            ? template.geneName            : geneName,
				template != null && ecNumber == null            ? template.ecNumber            : ecNumber,
				template != null && organism == null            ? template.organism            : organism,
				template != null && proteinName == null         ? template.proteinName         : proteinName,
				template != null && reviewed == null            ? template.reviewed            : reviewed,
				template != null && synonymGeneNames == null    ? template.synonymGeneNames    : synonymGeneNames,
				template != null && biologicalProcesses == null ? template.biologicalProcesses : biologicalProcesses,
				template != null && cellularComponents == null  ? template.cellularComponents  : cellularComponents,
				template != null && molecularFunctions == null  ? template.molecularFunctions  : molecularFunctions
			);

			if (template != null && orthologs == null)
				orthologs = template.orthologs;
			for (Protein ortholog : orthologs.values()) {
				uniProtEntry.addOrtholog(ortholog);
			}
			return uniProtEntry;
		}

	}

}

package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
import ch.picard.ppimapbuilder.data.organism.Organism;
import com.eclipsesource.json.JsonObject;

import java.util.*;

public class UniProtEntry extends Protein {

	private final LinkedHashSet<String> accessions;
	private final String geneName;
	private final Set<String> synonymGeneNames;
	private final String proteinName;
	private final String ecNumber;
	private final boolean reviewed;
	private final GeneOntologyTermSet geneOntologyTerms;

	private UniProtEntry(
			String uniprotId,
			LinkedHashSet<String> accessions,
			String geneName,
			String ecNumber,
			Organism organism,
			String proteinName,
			boolean reviewed,
			Set<String> synonymGeneNames,
			GeneOntologyTermSet geneOntologyTerms
	) {
		super(uniprotId, organism);
		this.accessions = accessions;
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
		this.synonymGeneNames = synonymGeneNames;
		this.geneOntologyTerms = geneOntologyTerms;
	}

	public GeneOntologyTermSet getGeneOntologyTerms() {
		return geneOntologyTerms;
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

	public boolean hasAccession(String uniProtID) {
		return uniProtID.equals(uniProtID) || accessions.contains(uniProtID);
	}

	public boolean isIdentical(UniProtEntry entry) {
		return accessions.equals(entry.accessions)
				&& organism.equals(entry.organism)
				&& synonymGeneNames.equals(entry.synonymGeneNames)
				&& geneOntologyTerms.equals(entry.geneOntologyTerms)
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

		private String uniprotId = null;
		private LinkedHashSet<String> accessions = null;
		private Organism organism = null;
		private String geneName = null;
		private Set<String> synonymGeneNames = null;
		private String proteinName = null;
		private String ecNumber = null;
		private Boolean reviewed = false;
		private GeneOntologyTermSet geneOntologyTerms = null;

		public Builder() {
			this((UniProtEntry) null);
		}

		public Builder(Protein protein) {
			if(protein != null) {
				uniprotId = protein.uniProtId;
				organism = protein.organism;
			}
		}

		/**
		 * Constructs a UniProtEntry.Builder using an existing UniProtEntry as a template.
		 */
		public Builder(UniProtEntry entry) {
			this((Protein) entry);
			if(entry != null) {
				accessions = new LinkedHashSet<String>(entry.accessions);
				geneName = entry.geneName;
				synonymGeneNames = new HashSet<String>(entry.synonymGeneNames);
				proteinName = entry.proteinName;
				ecNumber = entry.ecNumber;
				reviewed = entry.reviewed;
				geneOntologyTerms = new GeneOntologyTermSet(entry.geneOntologyTerms);
			}
		}

		/**
		 * Constructs a UniProtEntry.Builder using ther merging of existing UniProtEntry as a template.
		 */
		public Builder(UniProtEntry referenceEntry, UniProtEntry... entries) {
			this(referenceEntry, Arrays.asList(entries));
		}

		public Builder(UniProtEntry referenceEntry, Collection<UniProtEntry> entries) {
			this(referenceEntry);

			for (UniProtEntry entry : entries) {
				addAccessions(entry.accessions);
				addSynonymGeneNames(entry.synonymGeneNames);
				addGeneOntologyTerms(entry.geneOntologyTerms);
			}
		}

		public Builder setUniprotId(String uniprotId) {
			this.uniprotId = uniprotId;
			return this;
		}

		private LinkedHashSet<String> getOrCreateAccessions() {
			if(accessions != null) return accessions;
			return accessions = new LinkedHashSet<String>();
		}

		public Builder addAccession(String accession) {
			getOrCreateAccessions().add(accession);
			return this;
		}

		public Builder addAccessions(Collection<String> accessions) {
			getOrCreateAccessions().addAll(accessions);
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

		private Set<String> getOrCreateSynonymGeneNames() {
			if(synonymGeneNames != null) return synonymGeneNames;
			return synonymGeneNames = new HashSet<String>();
		}

		public Builder addSynonymGeneName(String synonymGeneName) {
			if(synonymGeneName != null)
				getOrCreateSynonymGeneNames().add(synonymGeneName);
			return this;
		}

		public Builder addSynonymGeneNames(Collection<String> synonymGeneNames) {
			for (String synonymGeneName : synonymGeneNames)
				addSynonymGeneName(synonymGeneName);
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

		private GeneOntologyTermSet getOrCreateGeneOntologyTerms() {
			if(geneOntologyTerms != null) return geneOntologyTerms;
			return geneOntologyTerms = new GeneOntologyTermSet();
		}

		public Builder addGeneOntologyTerms(Collection<GeneOntologyTerm> terms) {
			for (GeneOntologyTerm term : terms)
				addGeneOntologyTerm(term);
			return this;
		}

		public Builder addGeneOntologyTerm(GeneOntologyTerm term) {
			if(term != null)
				getOrCreateGeneOntologyTerms().add(term);
			return this;
		}

		public UniProtEntry build() {
			return new UniProtEntry(
					uniprotId,
					getOrCreateAccessions(),
					geneName,
					ecNumber,
					organism,
					proteinName,
					reviewed,
					getOrCreateSynonymGeneNames(),
					getOrCreateGeneOntologyTerms()//,
			);
		}

	}

}

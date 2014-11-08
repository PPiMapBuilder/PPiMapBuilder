package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTermSet;
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
	transient private final GeneOntologyTermSet geneOntologyTerms;
	transient private final Set<Protein> orthologs;

	private UniProtEntry(
			String uniprotId,
			LinkedHashSet<String> accessions,
			String geneName,
			String ecNumber,
			Organism organism,
			String proteinName,
			boolean reviewed,
			Set<String> synonymGeneNames,
			GeneOntologyTermSet geneOntologyTerms,
			Set<Protein> orthologs
	) {
		super(uniprotId, organism);
		this.accessions = accessions;
		this.proteinName = proteinName;
		this.ecNumber = ecNumber;
		this.geneName = geneName;
		this.reviewed = reviewed;
		this.synonymGeneNames = synonymGeneNames;
		this.geneOntologyTerms = geneOntologyTerms;
		this.orthologs = orthologs;
	}

	public Collection<Protein> getOrthologs(Organism organism) {
		final ArrayList<Protein> orthologs = new ArrayList<Protein>();
		for (Protein ortholog : this.orthologs)
			if(ortholog.getOrganism().equals(organism))
				orthologs.add(ortholog);
		return orthologs;
	}

	public Collection<Protein> getOrthologs() {
		return orthologs;
	}

	public void addOrthologs(Collection<? extends Protein> orthologs) {
		for (Protein ortholog : orthologs)
			addOrtholog(ortholog);
	}

	public void addOrtholog(Protein ortholog) {
		if(ortholog != null)
			orthologs.add(ortholog);
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
				&& orthologs.equals(entry.orthologs)
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
		private Boolean reviewed = null;
		private GeneOntologyTermSet geneOntologyTerms = null;
		private HashSet<Protein> orthologs = null;

		public Builder() {
			this((UniProtEntry) null);
		}

		/**
		 * Constructs a UniProtEntry.Builder using an existing UniProtEntry as a template.
		 */
		public Builder(UniProtEntry entry) {
			if(entry != null) {
				uniprotId = entry.uniProtId;
				accessions = new LinkedHashSet<String>(entry.accessions);
				organism = entry.organism;
				geneName = entry.geneName;
				synonymGeneNames = new HashSet<String>(entry.synonymGeneNames);
				proteinName = entry.proteinName;
				ecNumber = entry.ecNumber;
				reviewed = entry.reviewed;
				geneOntologyTerms = new GeneOntologyTermSet(entry.geneOntologyTerms);
				orthologs = new HashSet<Protein>(entry.orthologs);
			}
		}

		/**
		 * Constructs a UniProtEntry.Builder using ther merging of existing UniProtEntry as a template.
		 */
		public Builder(UniProtEntry referenceEntry, UniProtEntry... entries) {
			this(referenceEntry);

			for (UniProtEntry entry : entries) {
				addAccessions(entry.accessions);
				addAccession(entry.uniProtId);
				addSynonymGeneNames(entry.synonymGeneNames);
				addGeneOntologyTerms(entry.geneOntologyTerms);
				addOrthologs(entry.orthologs);
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

		private HashSet<Protein> getOrCreateOrthologs() {
			if(orthologs != null) return orthologs;
			return orthologs = new HashSet<Protein>();
		}

		public Builder addOrthologs(Collection<? extends Protein> orthologs) {
			for (Protein ortholog : orthologs)
				getOrCreateOrthologs().add(ortholog);
			return this;
		}

		public Builder addOrtholog(Protein ortholog) {
			if(ortholog != null)
				getOrCreateOrthologs().add(ortholog);
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
					getOrCreateGeneOntologyTerms(),
					getOrCreateOrthologs()
			);
		}

	}

}

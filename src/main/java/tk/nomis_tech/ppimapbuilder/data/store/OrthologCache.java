package tk.nomis_tech.ppimapbuilder.data.store;

import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class OrthologCache {

	private final File orthologCacheFolder;

	private final Map<Organism, Map<Organism, OrganismOrthologyPair>> indexOfOrganismOrthologyPair;

	protected OrthologCache() {
		orthologCacheFolder = new File(PMBStore.getPpiMapBuilderConfigurationFolder(), "ortholog-cache");

		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdir();

		indexOfOrganismOrthologyPair = loadOrCreateIndexOfOrganismOrthologyPair();
	}

	private Map<Organism, Map<Organism, OrganismOrthologyPair>> loadOrCreateIndexOfOrganismOrthologyPair() {
		//TODO : load form file if exists else instanciate
		return null;
	}

	public Protein getOrtholog(Protein sourceProt, Organism destOrg) {
		//TODO : search in indexOfOrganismOrthologyPair and ask for ortholog
		return null;
	}

	public File getOrthologCacheFolder() {
		return orthologCacheFolder;
	}

	class OrganismOrthologyPair implements Serializable {
		private final String cacheDataFileName;
		private final String cacheIndexFileName;

		private final Organism organismA;
		private final Organism organismB;

		private final Index index;

		OrganismOrthologyPair(Organism organismA, Organism organismB) {
			this.organismA = organismA;
			this.organismB = organismB;

			String identifier = organismA.getAbbrName() + "-" + organismB.getAbbrName();

			// Ortholog cache data file for this organism pair
			cacheDataFileName = identifier + ".dat";

			// Ortholog cache index file for this organism pair
			cacheIndexFileName = identifier + ".idx";
			index = loadOrCreateIndex();
		}

		private Index loadOrCreateIndex() {
			// TODO : load organism pair index if file exists else instanciate
			File cacheIndexFile = new File(
					PMBStore.getOrthologCache().getOrthologCacheFolder(),
					cacheIndexFileName
			);
			return null;
		}

		public void addOrtholog(String sourceProt, Organism sourceOrg, String destProt, Organism destOrg) {


			int sourceProtIndex = index.addProteinInOrganism(sourceProt, sourceOrg);
			int destProtIndex = index.addProteinInOrganism(destProt, destOrg);

			// TODO : append the couple of int index to the cache data file
		}

		public String getOrtholog(String uniProtId, Organism sourceOrg) {
			int sourceProtIndex = index.indexOfProteinInOrganism(uniProtId, sourceOrg);

			if (sourceProtIndex > 0) {

				//TODO: loop in file to seach protein
				return null;
			}
			return null;
		}

		/**
		 * Protein index for OrganismOrthologyPair
		 */
		class Index implements Serializable {
			private final Map<Organism, LinkedHashSet<String>> index;

			Index(Organism organismA, Organism organismB) {
				this.index = new HashMap<Organism, LinkedHashSet<String>>();
				this.index.put(organismA, new LinkedHashSet<String>());
				this.index.put(organismB, new LinkedHashSet<String>());
			}

			/**
			 * Adds a protein in the index of an organism
			 *
			 * @param protId
			 * @param organism
			 * @return the index of the newly inserted protein (just returning the index if the protein already existed); -1 if error (organism unknown)
			 */
			public int addProteinInOrganism(String protId, Organism organism) {
				LinkedHashSet<String> proteinIndex = index.get(organism);

				if ((proteinIndex == null))
					return -1;
				else if (proteinIndex.add(protId))
					return proteinIndex.size() - 1;
				else
					return indexOfProteinInOrganism(protId, organism);
			}

			public int indexOfProteinInOrganism(String protId, Organism organism) {
				LinkedHashSet<String> proteinIndex = index.get(organism);

				if (proteinIndex == null)
					return -1;

				int i = 0;
				for (String prot : proteinIndex) {
					if (prot.equals(protId))
						return i;
					i++;
				}
				return -1;
			}
		}
	}
}

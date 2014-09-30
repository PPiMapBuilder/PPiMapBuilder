package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.util.AppendingObjectOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Protein ortholog cache client for a pair of species. Linked to a file cache named "ORG1-ORG2.dat".
 */
public class SpeciesPairProteinOrthologCache extends AbstractProteinOrthologCacheClient implements Serializable {

	private static final long serialVersionUID = 2L;

	/**
	 * Indicates if this cache have been loaded entirely from InParanoid (Set to true at the end of an OrthoXML parsing)
	 */
	private Boolean full = false;

	private final CacheFile cacheDataFile;

	/**
	 * In memory cache of already read OrthologGroup
	 */
	private transient Map<Protein, OrthologGroup> readCache;

	protected SpeciesPairProteinOrthologCache(Organism organismA, Organism organismB) throws IOException {
		cacheDataFile = new CacheFile(organismA.getAbbrName() + "-" + organismB.getAbbrName() + ".dat");
	}

	@Override
	protected synchronized void addOrthologGroup(OrthologGroup orthologGroup) throws IOException {
		addOrthologGroup(Arrays.asList(orthologGroup), true);
	}

	private synchronized void addOrthologGroup(List<OrthologGroup> orthologGroups, boolean checkOrthologyExists) throws IOException {
		ObjectOutputStream out = null;
		try {
			if (!cacheDataFile.exists())
				out = new ObjectOutputStream(new FileOutputStream(cacheDataFile.getOrCreateFile()));
			else
				out = new AppendingObjectOutputStream(new FileOutputStream(cacheDataFile.getFile(), true));

			for (OrthologGroup orthologGroup : orthologGroups) {
				boolean ok = true;
				if (checkOrthologyExists) {
					OrthologGroup existingGroup = getOrthologGroup(orthologGroup);

					if (existingGroup != null)
						ok = false;
				}

				if (ok) out.writeObject(orthologGroup);
			}
		} finally {
			if (out != null) out.close();
		}
	}

	private OrthologGroup getOrthologGroup(OrthologGroup orthologGroup) throws IOException {
		for (Organism organism : orthologGroup.getOrganisms()) {
			for (Protein protein : orthologGroup.getProteins()) {
				if (!protein.getOrganism().equals(organism)) {
					OrthologGroup group = getOrthologGroup(protein, organism);

					if (group != null)
						return group;
				}
			}
		}

		return null;
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws IOException {
		if (!cacheDataFile.exists())
			return null;

		// Create or Check in memory cache before reading file cache
		if (readCache == null || readCache.size() == 0) {
			synchronized (this) {
				if(readCache == null)
					readCache = new HashMap<Protein, OrthologGroup>();

				ObjectInputStream in = null;
				try {
					in = new ObjectInputStream(new FileInputStream(cacheDataFile.getFile()));

					boolean EOF = false;
					while (!EOF) {
						try {
							OrthologGroup group = (OrthologGroup) in.readObject();

							for(Protein ortholog : group.getProteins()) {
								readCache.put(ortholog, group);
							}

						} catch (EOFException e) {
							EOF = true;
						} catch (ClassNotFoundException ignored) {}
					}
				} catch (FileNotFoundException ignored) {
				} finally {
					if (in != null) in.close();
				}
			}
		}

		return readCache.get(protein);
	}

	/**
	 * Clear both file cache and memory cache
	 */
	protected synchronized void clear() throws IOException {
		clearReadCache();
		cacheDataFile.clear();
		full = false;
	}

	public Loader newLoader() {
		return new Loader();
	}

	public boolean isFull() {
		if (cacheDataFile.exists())
			return full;
		else
			return (full = false);
	}

	/**
	 * Clear memory cache of OrthologGroup read from the file cache
	 */
	protected void clearReadCache() {
		if(readCache != null)
			readCache.clear();
	}

	/**
	 * Used to load an entire new cache into a SpeciesPairProteinOrthologCache
	 * (clears the old cache in the process).
	 */
	public class Loader {
		/**
		 * Loads a big group of orthologs into the cache
		 */
		public void load(List<OrthologGroup> orthologGroups) throws IOException {
			//Empty the cache and protein index
			SpeciesPairProteinOrthologCache.this.clear();

			//Load orthologs without checking if the orthology is already in cache
			SpeciesPairProteinOrthologCache.this.addOrthologGroup(orthologGroups, false);

			full = true;
		}
	}
}
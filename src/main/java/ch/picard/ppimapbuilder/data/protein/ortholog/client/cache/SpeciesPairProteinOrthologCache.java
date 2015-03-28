package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.AbstractProteinOrthologClient;
import ch.picard.ppimapbuilder.util.io.AppendingObjectOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Protein ortholog cache client for a pair of species. Linked to a file cache named "ORG1-ORG2.dat".
 */
public class SpeciesPairProteinOrthologCache extends AbstractProteinOrthologClient implements ProteinOrthologCacheClient, Serializable {

	private static final long serialVersionUID = 2L;

	/**
	 * Indicates if this cache have been loaded entirely from InParanoid
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
	public synchronized void addOrthologGroup(OrthologGroup orthologGroup) throws IOException {
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
				if (checkOrthologyExists && orthologGroupExist(orthologGroup))
					ok = false;

				if (ok) {
					out.writeObject(orthologGroup);
					clearReadCache();
				}
			}
		} finally {
			if (out != null) out.close();
		}
	}

	private boolean orthologGroupExist(OrthologGroup orthologGroup) throws IOException {
		OrthologGroup group = getOrthologGroup(
				orthologGroup.getProteins().get(0),
				orthologGroup.getOrganisms().get(0)
		);
		return group != null;
	}

	/**
	 * Get the ortholog group of a protein in the species pair. Here the organism parameter doesn't apply.
	 */
	@Override
	public synchronized OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws IOException {
		OrthologGroup result = null;

		// Create or Check in memory cache before reading file cache
		if (readCache == null || readCache.size() == 0) {
			if(readCache == null)
				readCache = new HashMap<Protein, OrthologGroup>();

			if (!cacheDataFile.exists())
				return null;

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

						if(group.contains(protein))
							result = group;

					} catch (EOFException e) {
						EOF = true;
					} catch (ClassNotFoundException ignored) {}
				}
			} catch (FileNotFoundException ignored) {
			} finally {
				if (in != null) in.close();
			}
		}

		return result == null ? readCache.get(protein) : result;
	}

	/**
	 * Clear both file cache and memory cache
	 */
	protected synchronized void clear() throws IOException {
		clearReadCache();
		cacheDataFile.clear();
		full = false;
	}

	/**
	 * Get a new loader to completely load the species pair cache (and clearing it before load).
	 */
	public Loader newLoader() {
		return new Loader();
	}

	/**
	 * Indicate whether the cache have been totally loaded from InParanoid or not.
	 * Caches declared as full are trusted to contains all possible orthologs in species pair.
	 * This is used to tell if the program has to retry the ortholog request using the InParanoid web service.
	 */
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
		 * Loads all orthologGroup of the species pair and declare the cache full.
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
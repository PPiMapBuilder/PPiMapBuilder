package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpeciesPairProteinOrthologCache implements ProteinOrthologCache, Serializable {

	private static final long serialVersionUID = -8952237235835456060L;

	private final String identifier;

	private transient ProteinOrthologIndex proteinOrthologIndex;

	private transient File cacheDataFile;

	protected SpeciesPairProteinOrthologCache(Organism organismA, Organism organismB) throws IOException {
		// Ortholog cache data file for this organism pair
		identifier = organismA.getAbbrName() + "-" + organismB.getAbbrName();

		// Ortholog cache proteinOrthologIndex file for this organism pair
		proteinOrthologIndex = new ProteinOrthologIndex(identifier);
	}

	private File getCacheDataFile() throws IOException {
		if (cacheDataFile == null) {
			cacheDataFile = new File(PMBSettings.getInstance().getOrthologCacheFolder(), identifier + ".dat");
		}
		return cacheDataFile;
	}

	/**
	 * Store a new orthology association between two proteins
	 * @param proteinA
	 * @param proteinB
	 * @throws IOException if error occurred during writing in cache
	 */
	@Override
	public synchronized void addOrthologGroup(Protein proteinA, Protein proteinB) throws IOException {
		List<Pair<? extends Protein>> proteinPairs = new ArrayList<Pair<? extends Protein>>();
		proteinPairs.add(new Pair<Protein>(proteinA, proteinB));
		addOrthologGroup(proteinPairs, true, true);
	}

	/**
	 * Stores new orthology associations between protein pairs
	 *
	 * @param proteinPairs         the list of protein pairs to store in cache
	 * @throws IOException if error occurred during writing in cache
	 */
	@Override
	public void addOrthologGroup(List<Pair<? extends Protein>> proteinPairs) throws IOException {
		addOrthologGroup(proteinPairs, true, true);
	}

	/**
	 * Stores new orthology associations between protein pairs
	 *
	 * @param proteinPairs         the list of protein pairs to store in cache
	 * @param saveProteinIndex     if true the protein index will be saved at each addition of new proteins
	 * @param checkOrthologyExists if true the method will check each ortholog group to see that they are not already
	 *                             existing in cache and thus avoid double-insertion
	 * @throws IOException if error occurred during writing in cache
	 */
	private synchronized void addOrthologGroup(List<Pair<? extends Protein>> proteinPairs, boolean saveProteinIndex, boolean checkOrthologyExists) throws IOException {
		DataOutputStream out = null;
		try {
			File orthologCacheFolder = PMBSettings.getInstance().getOrthologCacheFolder();
			if (!orthologCacheFolder.exists())
				orthologCacheFolder.mkdirs();
			if (!getCacheDataFile().exists())
				getCacheDataFile().createNewFile();

			out = new DataOutputStream(new FileOutputStream(getCacheDataFile(), true));

			for (Pair<? extends Protein> proteinPair : proteinPairs) {

				Protein proteinA = proteinPair.getFirst();
				Protein proteinB = proteinPair.getSecond();

				if (!checkOrthologyExists || getOrtholog(proteinA, proteinB.getOrganism()) == null) {
					int length = proteinOrthologIndex.size();

					//Add protein (or not if already exist) to protein index and get its index
					int sourceProtIndex = proteinOrthologIndex.addProtein(proteinA);
					int destProtIndex = proteinOrthologIndex.addProtein(proteinB);

					//At least one protein have been added => save the index
					Future<Void> f = null;
					if (saveProteinIndex && length < proteinOrthologIndex.size()) {
						f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
							@Override
							public Void call() throws Exception {
								proteinOrthologIndex.save();
								return null;
							}
						});
					}

					//Append orthologs

					out.writeInt(sourceProtIndex);
					out.writeInt(destProtIndex);

					//Wait for the index save to finish before continuing
					if (f != null) {
						try {
							f.get();
						} catch (ExecutionException e) {
							if (e.getCause() instanceof IOException)
								throw (IOException) e.getCause();
							e.printStackTrace();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		} finally {
			if (out != null) out.close();
		}
	}



	/**
	 * Get an ortholog of a protein from the cache
	 * @param protein
	 * @param organism
	 * @return
	 * @throws IOException if an error occurred during the reading of the cache
	 */
	@Override
	public synchronized Protein getOrtholog(Protein protein, Organism organism) throws IOException {
		if (proteinOrthologIndex == null) {
			proteinOrthologIndex = new ProteinOrthologIndex(identifier);
		}
		int sourceProtIndex = proteinOrthologIndex.indexOfProtein(protein);

		if (sourceProtIndex < 0)
			return null;

		Protein ortholog = null;

		DataInputStream in = null;

		try {
			in = new DataInputStream(new FileInputStream(getCacheDataFile()));

			boolean EOF = false;
			while (!EOF) {
				try {
					//Reading prot index pair one by one
					int protAIndex = in.readInt();
					int protBIndex = in.readInt();

					if (protAIndex == sourceProtIndex) {
						ortholog = proteinOrthologIndex.getProtein(protBIndex);
					} else if (protBIndex == sourceProtIndex) {
						ortholog = proteinOrthologIndex.getProtein(protAIndex);
					}

					if (ortholog != null && ortholog.getOrganism().equals(organism))
						break;
					else ortholog = null;
				} catch (EOFException e) {
					EOF = true;
				}
			}
		} catch (FileNotFoundException e) {
			return null;
		} finally {
			if (in != null) in.close();
		}

		return ortholog;
	}

	private synchronized void clearAll() throws IOException {
		proteinOrthologIndex.clear();
		if (getCacheDataFile().exists())
			getCacheDataFile().delete();
	}

	/**
	 * Used to load an entire new cache into a SpeciesPairProteinOrthologCache (clears the old cache in the process).
	 */
	public static class Loader {
		private final SpeciesPairProteinOrthologCache cache;
		private final List<Pair<Protein>> orthologGroups;

		public Loader(SpeciesPairProteinOrthologCache cache) throws IOException {
			this.cache = cache;
			orthologGroups = new ArrayList<Pair<Protein>>();
		}

		/**
		 * Loads a big group of orthologs into the cache
		 */
		public void load(List<Pair<? extends Protein>> orthologGroups) throws IOException {
			//Empty the cache and protein index
			this.cache.clearAll();

			//Load orthologs without automatic protein index saving and
			// without checking if the orthology is already in cache
			cache.addOrthologGroup(orthologGroups, false, false);

			//Save manually the protein index
			cache.proteinOrthologIndex.save();
		}
	}
}
package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import tk.nomis_tech.ppimapbuilder.data.client.AbstractProteinOrthologClient;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.util.FileUtil;

import java.io.*;
import java.util.*;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism (doesn't reflect reality).
 * The orthology links stored in PPiMapBuilder's ortholog cache don't have any score of quality.
 */
public class ProteinOrthologCacheClient extends AbstractProteinOrthologClient {

	private static ProteinOrthologCacheClient _instance;
	private File orthologCacheIndexFile;
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> orthologCacheIndex;

	private ProteinOrthologCacheClient() throws IOException {
		super();
		orthologCacheIndexFile = new File(PMBSettings.getInstance().getOrthologCacheFolder(), "ortholog-cache.idx");

		if (orthologCacheIndexFile.exists())
			orthologCacheIndex = load();
		else {
			orthologCacheIndex = new HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>();
			save();
		}
	}

	public static ProteinOrthologCacheClient getInstance() throws IOException {
		if (_instance == null)
			_instance = new ProteinOrthologCacheClient();
		return _instance;
	}

	/**
	 * Loads or creates the PMB orthology cache index form file "ortholog-cache.idx"
	 *
	 * @return
	 * @throws IOException
	 */
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> load() throws IOException {
		HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> index = null;

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(orthologCacheIndexFile));

			try {
				index = (HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>) in.readObject();
			} catch (InvalidClassException e) {
				// Old cache that is incompatible with the current version
				//  => emptying old cache
				return clear();
			}
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
		} finally {
			if (in != null) in.close();
		}

		return index;
	}

	/**
	 * Clear all ortholog cache files and return an empty cache index.
	 */
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> clear() throws IOException {
		FileUtil.recursiveDelete(PMBSettings.getInstance().getOrthologCacheFolder());
		return new HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>();
	}


	/**
	 * Saves the ortholog cache index in file "ortholog-cache.idx"
	 *
	 * @throws IOException
	 */
	private synchronized void save() throws IOException {
		File orthologCacheFolder = PMBSettings.getInstance().getOrthologCacheFolder();
		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdirs();
		if (!orthologCacheIndexFile.exists())
			orthologCacheIndexFile.createNewFile();

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(orthologCacheIndexFile));

			out.writeObject(orthologCacheIndex);
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) out.close();
		}
	}

	/**
	 * Gets the orthologous protein of the given protein in the specified destination organism from the ortholog cache
	 *
	 * @param protein   the protein of interest
	 * @param organismB the organism of the desired ortholog
	 * @return the orthologous protein
	 */
	public Protein getOrtholog(Protein protein, Organism organismB) throws IOException {
		try {
			SpeciesPairProteinOrthologCache cache = this.orthologCacheIndex.get(protein.getOrganism()).get(organismB);
			Protein ortholog = cache.getOrtholog(protein, organismB);
			if (protein instanceof UniProtEntry) {
				((UniProtEntry) protein).addOrtholog(ortholog);
			}
			return ortholog;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Adds an ortholog group into the PMB ortholog cache.
	 * The order in which the two protein are given doesn't matter (their order will be switched according to the
	 * alphabetical order of their organisms).
	 *
	 * @param proteinA
	 * @param proteinB
	 * @throws IOException
	 */
	public void addOrthologGroup(Protein proteinA, Protein proteinB) throws IOException {
		//Sort input to have always first organism ahead alphabetically
		List<Protein> prots = Arrays.asList(proteinA, proteinB);
		Collections.sort(prots, new Comparator<Protein>() {
			@Override
			public int compare(Protein o1, Protein o2) {
				return o1.getOrganism().compareTo(o2.getOrganism());
			}
		});

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = getSpeciesPairProteinOrthologCache(prots.get(0).getOrganism(), prots.get(1).getOrganism());

		//Add orthology to the organism pair orthology cache
		cache.addOrthologGroup(prots.get(0), prots.get(1));
		save();
	}

	/**
	 * Gets the species pair ortholog cache from the ortholog index or create it using a species couple.
	 *
	 * @param organismA
	 * @param organismB
	 * @return
	 * @throws IOException
	 */
	private SpeciesPairProteinOrthologCache getSpeciesPairProteinOrthologCache(Organism organismA, Organism organismB) throws IOException {
		//First organism exists in index?
		HashMap<Organism, SpeciesPairProteinOrthologCache> d = this.orthologCacheIndex.get(organismA);
		if (d == null) {
			//Doesn't exist, create an index entry
			d = new HashMap<Organism, SpeciesPairProteinOrthologCache>();
			this.orthologCacheIndex.put(organismA, d);
		}

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = d.get(organismB);
		if (cache == null) {
			//Doesn't exist, create a cache
			cache = new SpeciesPairProteinOrthologCache(organismA, organismB);
			d.put(organismB, cache);

			HashMap<Organism, SpeciesPairProteinOrthologCache> f = this.orthologCacheIndex.get(organismB);
			if (f == null) {
				f = new HashMap<Organism, SpeciesPairProteinOrthologCache>();
				this.orthologCacheIndex.put(organismB, f);
			}
			f.put(organismA, cache);

			//Save index
			save();
		}

		return cache;
	}

}

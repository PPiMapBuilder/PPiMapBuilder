package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import com.google.common.collect.Sets;
import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import ch.picard.ppimapbuilder.data.settings.PMBSettings;
import ch.picard.ppimapbuilder.util.FileUtil;

import java.io.*;
import java.util.*;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of
 * two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism.
 * The orthology links actually stores multiple orthologs and scores in an OrhtologGroup but only the ortholog with
 * highest score is currently accessible
 */
public class PMBProteinOrthologCacheClient extends AbstractProteinOrthologCacheClient {

	private static PMBProteinOrthologCacheClient _instance;
	//private File orthologCacheIndexFile;
	private CacheFile orthologCacheIndexFile;
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> orthologCacheIndex;

	/**
	 * Maximum number of species pair cache that can store a reading cache.
	 */
	public static final int MAX_NB_MEMORY_CACHE = 4;

	/**
	 * List of SpeciesPairProteinOrthologCache having memory cache
	 */
	private final ArrayList<SpeciesPairProteinOrthologCache> speciesPairMemoryCached;

	private PMBProteinOrthologCacheClient() throws IOException {
		super();

		orthologCacheIndexFile = new CacheFile("ortholog-cache.idx");

		//orthologCacheIndexFile = new File(PMBSettings.getInstance().getOrthologCacheFolder(), "ortholog-cache.idx");

		if (orthologCacheIndexFile.exists())
			orthologCacheIndex = load();
		else {
			orthologCacheIndex = empty();
			save();
		}

		speciesPairMemoryCached = new ArrayList<SpeciesPairProteinOrthologCache>();
	}

	public static synchronized PMBProteinOrthologCacheClient getInstance() {
		if (_instance == null) {
			try {
				_instance = new PMBProteinOrthologCacheClient();
			} catch (IOException e) {
				return null;
			}
		}
		return _instance;
	}

	/**
	 * Loads or creates the PMB orthology cache index form file "ortholog-cache.idx"
	 */
	@SuppressWarnings("unchecked")
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> load() throws IOException {
		HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> index = null;

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(orthologCacheIndexFile.getFile()));

			try {
				index = (HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>) in.readObject();
			} catch (InvalidClassException e) {
				// Old cache that is incompatible with the current version
				//  => emptying old cache, start fresh
				return empty();
			}
		} catch (Throwable e) {
			return empty();
		} finally {
			if (in != null) in.close();
		}

		return index;
	}

	/**
	 * Clear all ortholog cache files and return an empty cache index.
	 */
	public HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> empty() throws IOException {
		FileUtil.recursiveDelete(PMBSettings.getInstance().getOrthologCacheFolder());
		if (orthologCacheIndex != null) {
			orthologCacheIndex.clear();
			return orthologCacheIndex;
		} else return new HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>();
	}

	/**
	 * Remove all file cache of protein orthology in the specified organism.
	 */
	public void emptyCacheLinkedToOrganism(Organism organism) {
		for(SpeciesPairProteinOrthologCache cache : orthologCacheIndex.get(organism).values()) {
			try {
				cache.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the ortholog cache index in file "ortholog-cache.idx".
	 */
	public synchronized void save() throws IOException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(orthologCacheIndexFile.getOrCreateFile()));

			out.writeObject(orthologCacheIndex);
		} finally {
			if (out != null) out.close();
		}
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		try {
			return getSpeciesPairProteinOrthologCache(
				protein.getOrganism(),
				organism
			).getOrthologGroup(protein, organism);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Adds an ortholog group into the PMB ortholog cache.
	 * The order in which the two protein are given doesn't matter (their order will be switched according to the
	 * alphabetical order of their organisms).
	 */
	@Override
	public void addOrthologGroup(OrthologGroup orthologGroup) throws IOException {
		//Sort input to have always first organism ahead alphabetically
		List<Organism> orgs = orthologGroup.getOrganisms();

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = getSpeciesPairProteinOrthologCache(orgs.get(0), orgs.get(1));

		//Add orthology to the organism pair orthology cache
		cache.addOrthologGroup(orthologGroup);
	}

	/**
	 * Gets the species pair ortholog cache from the ortholog index or create it using a species pair.
	 */
	public synchronized SpeciesPairProteinOrthologCache getSpeciesPairProteinOrthologCache(Organism organismA, Organism organismB) throws IOException {
		//First organism exists in index?
		HashMap<Organism, SpeciesPairProteinOrthologCache> d = this.orthologCacheIndex.get(organismA);
		if (d == null) {
			//Doesn't exists, create an index entry
			this.orthologCacheIndex.put(organismA, (d = new HashMap<Organism, SpeciesPairProteinOrthologCache>()));
		}

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = d.get(organismB);
		if (cache == null) {
			//Doesn't exists, create a cache
			cache = new SpeciesPairProteinOrthologCache(organismA, organismB);

			d.put(organismB, cache);

			HashMap<Organism, SpeciesPairProteinOrthologCache> f = this.orthologCacheIndex.get(organismB);
			if (f == null)
				this.orthologCacheIndex.put(organismB, (f = new HashMap<Organism, SpeciesPairProteinOrthologCache>()));

			f.put(organismA, cache);

			//Save index
			save();
		}

		if(!speciesPairMemoryCached.contains(cache)) {
			if (speciesPairMemoryCached.size() > MAX_NB_MEMORY_CACHE) {
				SpeciesPairProteinOrthologCache cache1 = speciesPairMemoryCached.get(0);
				cache1.clearReadCache();
				speciesPairMemoryCached.remove(cache1);
			}
			speciesPairMemoryCached.add(cache);
		}

		return cache;
	}

	public boolean isFull(Organism organismA, Organism organismB) throws IOException {
		return getSpeciesPairProteinOrthologCache(organismA, organismB).isFull();
	}

	/**
	 * Clear memory cache built when reading ortholog file cache
	 */
	public void clearMemoryCache() {
		for(SpeciesPairProteinOrthologCache cache : speciesPairMemoryCached) {
			cache.clearReadCache();
		}
		speciesPairMemoryCached.clear();
	}

	/**
	 * Calculate the percent of loaded orthologs
	 */
	public double getPercentLoadedFromOrganisms(List<Organism> organisms) throws IOException {
		Set<Pair<Organism>> possibleCombinations = OrganismUtils.createCombinations(organisms);

		Set<Pair<Organism>> loadedCombinations = new HashSet<Pair<Organism>>();
		for (Pair<Organism> combination : possibleCombinations) {
			Organism organismA = combination.getFirst();
			Organism organismB = combination.getSecond();
			if (isFull(organismA, organismB))
				loadedCombinations.add(combination);
		}

		Sets.SetView<Pair<Organism>> intersection = Sets.intersection(possibleCombinations, loadedCombinations);

		return (double) intersection.size() / (double) possibleCombinations.size() * 100.0;
	}
}

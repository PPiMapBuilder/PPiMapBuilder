package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache;

import com.google.common.collect.Sets;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismUtils;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.OrthologGroup;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;
import tk.nomis_tech.ppimapbuilder.util.FileUtil;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism (doesn't reflect reality).
 * The orthology links stored in PPiMapBuilder's ortholog cache don't have any score of quality.
 */
public class PMBProteinOrthologCacheClient extends AbstractProteinOrthologCacheClient {

	private static PMBProteinOrthologCacheClient _instance;
	//private File orthologCacheIndexFile;
	private CacheFile orthologCacheIndexFile;
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> orthologCacheIndex;

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
	}

	public static PMBProteinOrthologCacheClient getInstance() throws IOException {
		if (_instance == null)
			_instance = new PMBProteinOrthologCacheClient();
		return _instance;
	}

	/**
	 * Loads or creates the PMB orthology cache index form file "ortholog-cache.idx"
	 */
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
	public HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> empty() throws IOException {
		FileUtil.recursiveDelete(PMBSettings.getInstance().getOrthologCacheFolder());
		if (orthologCacheIndex != null) {
			orthologCacheIndex.clear();
			return orthologCacheIndex;
		} else return new HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>();
	}

	/**
	 * Saves the ortholog cache index in file "ortholog-cache.idx"
	 */
	public synchronized void save() throws IOException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(orthologCacheIndexFile.getOrCreateFile()));

			out.writeObject(orthologCacheIndex);
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) out.close();
		}
	}

	@Override
	public OrthologGroup getOrthologGroup(Protein protein, Organism organism) throws Exception {
		try {
			SpeciesPairProteinOrthologCache cache = getSpeciesPairProteinOrthologCache(protein.getOrganism(), organism);
			return cache.getOrthologGroup(protein, organism);
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
	 * Gets the species pair ortholog cache from the ortholog index or create it using a species couple.
	 */
	public synchronized SpeciesPairProteinOrthologCache getSpeciesPairProteinOrthologCache(Organism organismA, Organism organismB) throws IOException {
		//First organism exists in index?
		HashMap<Organism, SpeciesPairProteinOrthologCache> d = this.orthologCacheIndex.get(organismA);
		if (d == null)
			//Doesn't exist, create an index entry
			this.orthologCacheIndex.put(organismA, (d = new HashMap<Organism, SpeciesPairProteinOrthologCache>()));

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = d.get(organismB);
		if (cache == null) {
			//Doesn't exist, create a cache
			cache = new SpeciesPairProteinOrthologCache(organismA, organismB);
			d.put(organismB, cache);

			HashMap<Organism, SpeciesPairProteinOrthologCache> f = this.orthologCacheIndex.get(organismB);
			if (f == null)
				this.orthologCacheIndex.put(organismB, (f = new HashMap<Organism, SpeciesPairProteinOrthologCache>()));

			f.put(organismA, cache);

			//Save index
			save();
		}

		return cache;
	}

	public boolean isFull(Organism organismA, Organism organismB) throws IOException {
		return getSpeciesPairProteinOrthologCache(organismA, organismB).isFull();
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

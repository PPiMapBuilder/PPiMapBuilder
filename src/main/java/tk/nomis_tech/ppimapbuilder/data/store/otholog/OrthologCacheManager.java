package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.store.Organism;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

import java.io.*;
import java.util.*;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism (doesn't reflect reality).
 * The orthology links stored in PPiMapBuilder's ortholog cache don't have any score of quality.
 */
public class OrthologCacheManager {

	private File orthologCacheIndexFile;
	private HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> orthologCacheIndex;
	private File orthologCacheFolder;

	public OrthologCacheManager() throws IOException {
		orthologCacheFolder = new File(PMBStore.getPpiMapBuilderConfigurationFolder(), "ortholog-cache");

		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdir();

		orthologCacheIndexFile = new File(orthologCacheFolder, "ortholog-cache.idx");

		if (orthologCacheIndexFile.exists())
			orthologCacheIndex = load();
		else {
			orthologCacheIndex = new HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>();
			save();
		}
	}

	/**
	 * Loads or creates the PMB orthology cache index form file "ortholog-cache.idx"
	 *
	 * @return
	 * @throws IOException
	 */
	private HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> load() throws IOException {
		HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> index = null;

		FileInputStream fileIn = null;
		ObjectInputStream in = null;

		try {
			fileIn = new FileInputStream(orthologCacheIndexFile);
			in = new ObjectInputStream(fileIn);

			index = (HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>) in.readObject();
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
		} finally {
			if (in != null) in.close();
			if (fileIn != null) fileIn.close();
		}

		return index;
	}

	/**
	 * Saves the ortholog cache index in file "ortholog-cache.idx"
	 *
	 * @throws IOException
	 */
	private synchronized void save() throws IOException {
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
	 * @param proteinA  the protein of interest
	 * @param organismB the organism of the desired ortholog
	 * @return the orthologous protein
	 */
	public Protein getOrtholog(Protein proteinA, Organism organismB) throws IOException {
		try {
			OrganismPairOrthologCache cache = this.orthologCacheIndex.get(proteinA.getOrganism()).get(organismB);
			return cache.getOrtholog(proteinA, organismB);
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

		//First organism exists in index?
		HashMap<Organism, OrganismPairOrthologCache> d = this.orthologCacheIndex.get(prots.get(0).getOrganism());
		if (d == null) {
			//Doesn't exist, create an index entry
			d = new HashMap<Organism, OrganismPairOrthologCache>();
			this.orthologCacheIndex.put(prots.get(0).getOrganism(), d);
		}

		//Second organism leads to the organism pair orthology cache?
		OrganismPairOrthologCache cache = d.get(prots.get(1).getOrganism());
		if (cache == null) {
			//Doesn't exist, create a cache
			cache = new OrganismPairOrthologCache(prots.get(0).getOrganism(), prots.get(1).getOrganism());
			d.put(prots.get(1).getOrganism(), cache);

			HashMap<Organism, OrganismPairOrthologCache> f = this.orthologCacheIndex.get(prots.get(1).getOrganism());
			if (f == null) {
				f = new HashMap<Organism, OrganismPairOrthologCache>();
				this.orthologCacheIndex.put(prots.get(1).getOrganism(), f);
			}
			f.put(prots.get(0).getOrganism(), cache);
		}

		//Add orthology to the organism pair orthology cache
		cache.addOrthologGroup(prots.get(0), prots.get(1));
		save();
	}

	public File getOrthologCacheFolder() {
		return orthologCacheFolder;
	}

	//For test purpose only
	protected void setOrthologCacheFolder(File orthologCacheFolder) throws IOException {
		this.orthologCacheFolder = orthologCacheFolder;
		orthologCacheIndexFile = new File(orthologCacheFolder, "ortholog-cache.idx");

		if (orthologCacheIndexFile.exists())
			orthologCacheIndex = load();
		else {
			orthologCacheIndex = new HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>();
			save();
		}
	}
}

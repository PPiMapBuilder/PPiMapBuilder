package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

import java.io.*;
import java.util.*;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism (doesn't reflect reality).
 * The orthology links stored in PPiMapBuilder's ortholog cache don't have any score of quality.
 */
public class OrthologCacheManager {

	private final File orthologCacheFolder;
	private final File orthologCacheIndexFile;

	private final HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> orthologCacheIndex;

	public OrthologCacheManager() throws IOException {
		orthologCacheFolder = new File(PMBStore.getPpiMapBuilderConfigurationFolder(), "ortholog-cache");

		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdir();

		orthologCacheIndexFile = new File(orthologCacheFolder, "ortholog-cache.idx");

		orthologCacheIndex = loadOrCreateIndex();
	}

	/**
	 * Loads or creates the PMB orthology cache index form file "ortholog-cache.idx"
	 * @return
	 * @throws IOException
	 */
	private HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> loadOrCreateIndex() throws IOException {
		HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> index = null;
		if (!orthologCacheIndexFile.exists()) {
			return new HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>();
		}

		FileInputStream fileIn = null;
		ObjectInputStream in = null;

		try {
			fileIn = new FileInputStream(orthologCacheIndexFile);
			in = new ObjectInputStream(fileIn);

			index = (HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>) in.readObject();
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			//TODO: Do we handle bad file
		} finally {
			if (in != null) in.close();
			if (fileIn != null) fileIn.close();
		}

		if (orthologCacheIndex == null) {
			index = new HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>>();
			saveIndex(index);
		}
		return index;
	}

	private void saveIndex() throws IOException {
		saveIndex(this.orthologCacheIndex);
	}

	/**
	 * Saves the ortholog cache index in file "ortholog-cache.idx"
	 * @param index
	 * @throws IOException
	 */
	private void saveIndex(HashMap<Organism, HashMap<Organism, OrganismPairOrthologCache>> index) throws IOException {
		if (!orthologCacheIndexFile.exists())
			orthologCacheIndexFile.createNewFile();

		FileOutputStream fileOut = null;
		ObjectOutputStream out = null;

		try {
			fileOut = new FileOutputStream(orthologCacheIndexFile);
			out = new ObjectOutputStream(fileOut);

			out.writeObject(index);
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) out.close();
			if (fileOut != null) fileOut.close();
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
		//Sort input to have always first organism ahead alphabetically
		List<Protein> prots = Arrays.asList(proteinA, new Protein("", organismB));
		Collections.sort(prots, new Comparator<Protein>() {
			@Override
			public int compare(Protein o1, Protein o2) {
				return o1.getOrganism().compareTo(o2.getOrganism());
			}
		});

		try {
			OrganismPairOrthologCache cache = this.orthologCacheIndex.get(prots.get(0).getOrganism()).get(prots.get(1).getOrganism());
			return cache.getOrtholog(prots.get(0), prots.get(1).getOrganism());
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Adds an ortholog association between two protein into the PMB ortholog cache.
	 * The order in which the two protein are given doesn't matter (their order will be switched according to the
	 * alphabetical order of their organisms).
	 *
	 * @param proteinA
	 * @param proteinB
	 * @throws IOException
	 */
	public void addOrtholog(Protein proteinA, Protein proteinB) throws IOException {
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

		//Second organims lead to the organism pair othology cache?
		OrganismPairOrthologCache cache = d.get(prots.get(1).getOrganism());
		if (cache == null) {
			//Doesn't exist, create a cache
			cache = new OrganismPairOrthologCache(prots.get(0).getOrganism(), prots.get(1).getOrganism());
			d.put(prots.get(1).getOrganism(), cache);
		}

		//Add orthology to the organism pair orthology cache
		cache.addOrthologGroup(prots.get(0), prots.get(1));
	}

	public File getOrthologCacheFolder() {
		return orthologCacheFolder;
	}


}

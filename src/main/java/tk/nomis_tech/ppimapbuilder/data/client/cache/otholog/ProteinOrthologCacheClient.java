package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import tk.nomis_tech.ppimapbuilder.data.client.ProteinOrthologClient;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.UniProtEntry;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.*;
import java.util.*;

/**
 * The PPiMapBuilder protein orthology cache uses a simplified representation of protein orthology between proteins of two different species.
 * In the current model, a protein can have zero or one ortholog protein in another organism (doesn't reflect reality).
 * The orthology links stored in PPiMapBuilder's ortholog cache don't have any score of quality.
 */
public class ProteinOrthologCacheClient extends ProteinOrthologClient {

	private static ProteinOrthologCacheClient _instance;
	private File orthologCacheIndexFile;
	private HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>> orthologCacheIndex;


	private ProteinOrthologCacheClient() throws IOException {
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

		FileInputStream fileIn = null;
		ObjectInputStream in = null;

		try {
			fileIn = new FileInputStream(orthologCacheIndexFile);
			in = new ObjectInputStream(fileIn);

			index = (HashMap<Organism, HashMap<Organism, SpeciesPairProteinOrthologCache>>) in.readObject();
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
	 * @param protein   the protein of interest
	 * @param organismB the organism of the desired ortholog
	 * @return the orthologous protein
	 */
	public Protein getOrtholog(Protein protein, Organism organismB) throws IOException {
		try {
			SpeciesPairProteinOrthologCache cache = this.orthologCacheIndex.get(protein.getOrganism()).get(organismB);
			Protein ortholog = cache.getOrtholog(protein, organismB);
			if(protein instanceof UniProtEntry) {
				((UniProtEntry)protein).addOrtholog(ortholog);
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

		//First organism exists in index?
		HashMap<Organism, SpeciesPairProteinOrthologCache> d = this.orthologCacheIndex.get(prots.get(0).getOrganism());
		if (d == null) {
			//Doesn't exist, create an index entry
			d = new HashMap<Organism, SpeciesPairProteinOrthologCache>();
			this.orthologCacheIndex.put(prots.get(0).getOrganism(), d);
		}

		//Second organism leads to the organism pair orthology cache?
		SpeciesPairProteinOrthologCache cache = d.get(prots.get(1).getOrganism());
		if (cache == null) {
			//Doesn't exist, create a cache
			cache = new SpeciesPairProteinOrthologCache(prots.get(0).getOrganism(), prots.get(1).getOrganism());
			d.put(prots.get(1).getOrganism(), cache);

			HashMap<Organism, SpeciesPairProteinOrthologCache> f = this.orthologCacheIndex.get(prots.get(1).getOrganism());
			if (f == null) {
				f = new HashMap<Organism, SpeciesPairProteinOrthologCache>();
				this.orthologCacheIndex.put(prots.get(1).getOrganism(), f);
			}
			f.put(prots.get(0).getOrganism(), cache);
		}

		//Add orthology to the organism pair orthology cache
		cache.addOrthologGroup(prots.get(0), prots.get(1));
		save();
	}
}

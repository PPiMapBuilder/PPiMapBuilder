package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import org.apache.commons.collections.set.ListOrderedSet;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.*;

/**
 * Protein proteinIndex for SpeciesPairProteinOrthologCache.
 * This index stores protein by integer index (by order of insert).
 */
public class ProteinOrthologIndex {
	/**
	 * ListOrderedSet objects, as their name indicates, are hybrid of list (sequence with index) and set (no duplicates)
	 * which keeps insertion order.
	 */
	protected final ListOrderedSet proteinIndex;

	private final File proteinIndexFile;

	private final String fileName;

	protected ProteinOrthologIndex(String fileName) throws IOException {
		this.fileName = fileName;

		this.proteinIndexFile = new File(PMBSettings.getInstance().getOrthologCacheFolder(), fileName + ".idx");
		if (this.proteinIndexFile.exists())
			this.proteinIndex = load();
		else {
			this.proteinIndex = new ListOrderedSet();
			save();
		}
	}

	private synchronized ListOrderedSet load() throws IOException {
		ObjectInputStream in = null;

		try {
			in = new ObjectInputStream(new FileInputStream(proteinIndexFile));

			return (ListOrderedSet) in.readObject();

		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			//TODO: treat case when ProteinOrthologIndex is unrecognized
		} finally {
			if (in != null) in.close();
		}
		return null;
	}

	public synchronized void save() throws IOException {
		ObjectOutput out = null;

		// Make sure index file exists
		File orthologCacheFolder = PMBSettings.getInstance().getOrthologCacheFolder();
		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdirs();
		if (!proteinIndexFile.exists())
			proteinIndexFile.createNewFile();

		try {

			out = new ObjectOutputStream(new FileOutputStream(proteinIndexFile));

			out.writeObject(proteinIndex);

		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) out.close();
		}
	}


	public int indexOfProtein(Protein protein) {
		try {
			return proteinIndex.indexOf(protein);
		} catch (NullPointerException e) {
			return -1;
		}
	}

	/**
	 * Gets the protein at the given index
	 *
	 * @param index
	 * @return the requested protein or null if doesn't exits
	 */
	public Protein getProtein(int index) {
		try {
			return (Protein) proteinIndex.get(index);
		} catch (NullPointerException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Adds a protein in the protein index
	 *
	 * @param protein
	 * @return the index of the inserted protein (even if the protein already existed)
	 */
	public int addProtein(Protein protein) throws IOException {
		if (proteinIndex.add(protein))
			return this.proteinIndex.size() - 1;
		else
			return indexOfProtein(protein);
	}

	@Override
	public String toString() {
		return proteinIndex.toString();
	}

	public int size() {
		return proteinIndex.size();
	}
}
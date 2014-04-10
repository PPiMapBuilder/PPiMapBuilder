package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import tk.nomis_tech.ppimapbuilder.data.Organism;
import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

import java.io.*;

public class OrganismPairOrthologCache implements Serializable {

	private final String cacheDataFileName;
	private final String proteinIndexFileName;

	private transient ProteinIndex proteinIndex;

	private transient File proteinIndexFile;
	private transient File cacheDataFile;

	public OrganismPairOrthologCache(Organism organismA, Organism organismB) throws IOException {
		// Ortholog cache data file for this organism pair
		cacheDataFileName = organismA.getAbbrName() + "-" + organismB.getAbbrName() + ".dat";
		proteinIndexFileName = organismA.getAbbrName() + "-" + organismB.getAbbrName() + ".idx";

		// Ortholog cache proteinIndex file for this organism pair
		proteinIndex = new ProteinIndex(organismA, organismB);
		saveProteinIndex();
	}

	private File getProteinIndexFile() throws IOException {
		if (proteinIndexFile == null)
			proteinIndexFile = new File(PMBStore.getInstance().getOrthologCacheManager().getOrthologCacheFolder(), proteinIndexFileName);
		return proteinIndexFile;
	}

	private File getCacheDataFile() throws IOException {
		if (cacheDataFile == null)
			cacheDataFile = new File(PMBStore.getInstance().getOrthologCacheManager().getOrthologCacheFolder(), cacheDataFileName);
		return cacheDataFile;
	}

	private void loadProteinIndex() throws IOException {
		FileInputStream fileIn = null;
		ObjectInputStream in = null;

		try {
			fileIn = new FileInputStream(getProteinIndexFile());
			in = new ObjectInputStream(fileIn);

			proteinIndex = (ProteinIndex) in.readObject();

		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			//TODO: treat case when ProteinIndex is unrecognized
		} finally {
			if (in != null) in.close();
			if (fileIn != null) fileIn.close();
		}
	}

	private void saveProteinIndex() throws IOException {
		FileOutputStream fileOut = null;
		ObjectOutput out = null;

		try {
			fileOut = new FileOutputStream(getProteinIndexFile());
			out = new ObjectOutputStream(fileOut);

			out.writeObject(proteinIndex);

		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) fileOut.close();
			if (fileOut != null) fileOut.close();
		}
	}

	public void addOrthologGroup(Protein proteinA, Protein proteinB) throws IOException {
		if (getOrtholog(proteinA, proteinB.getOrganism()) == null) {
			int sourceProtIndex = proteinIndex.addProtein(proteinA);
			int destProtIndex = proteinIndex.addProtein(proteinB);

			File cacheDataFile = new File(PMBStore.getInstance().getOrthologCacheManager().getOrthologCacheFolder(), cacheDataFileName);

			//Append prot index pair to file
			FileOutputStream fileOut = null;
			DataOutputStream out = null;

			try {
				fileOut = new FileOutputStream(cacheDataFile, true);
				out = new DataOutputStream(fileOut);

				out.writeInt(sourceProtIndex);
				out.writeInt(destProtIndex);

			} catch (IOException e) {
				throw e;
			} finally {
				if (out != null) fileOut.close();
				if (fileOut != null) fileOut.close();
			}
		}
	}

	public Protein getOrtholog(Protein proteinA, Organism organismB) throws IOException {
		int sourceProtIndex = proteinIndex.indexOfProtein(proteinA);

		if (sourceProtIndex < 0)
			return null;

		File cacheDataFile = new File(PMBStore.getInstance().getOrthologCacheManager().getOrthologCacheFolder(), cacheDataFileName);

		Protein destProt = null;

		FileInputStream fileIn = null;
		DataInputStream in = null;

		try {
			fileIn = new FileInputStream(cacheDataFile);
			in = new DataInputStream(fileIn);

			boolean EOF = false;
			while (!EOF) {
				try {
					//Reading prot index pair one by one
					int protAIndex = in.readInt();
					int protBIndex = in.readInt();

					if (protAIndex == sourceProtIndex) {
						destProt = proteinIndex.getProtein(protBIndex, organismB);
					} else if (protBIndex == sourceProtIndex) {
						destProt = proteinIndex.getProtein(protAIndex, organismB);
					}

					if (destProt != null) break;
				} catch (EOFException e) {
					EOF = true;
				}
			}

		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) fileIn.close();
			if (fileIn != null) fileIn.close();
		}

		return destProt;
	}
}
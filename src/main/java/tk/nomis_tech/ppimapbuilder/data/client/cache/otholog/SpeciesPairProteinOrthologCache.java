package tk.nomis_tech.ppimapbuilder.data.client.cache.otholog;

import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SpeciesPairProteinOrthologCache implements Serializable {

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

	public synchronized void addOrthologGroup(Protein proteinA, Protein proteinB) throws IOException {
		if (getOrtholog(proteinA, proteinB.getOrganism()) == null) {
			//Add protein (or not if already exist) to protein index and get its index
			int sourceProtIndex = proteinOrthologIndex.addProtein(proteinA);
			int destProtIndex = proteinOrthologIndex.addProtein(proteinB);

			Future f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					proteinOrthologIndex.save();
					return null;
				}
			});

			//Append prot index pair to file
			DataOutputStream out = null;

			try {
				File orthologCacheFolder = PMBSettings.getInstance().getOrthologCacheFolder();
				if(!orthologCacheFolder.exists())
					orthologCacheFolder.mkdirs();
				if(!getCacheDataFile().exists())
					getCacheDataFile().createNewFile();

				out = new DataOutputStream(new FileOutputStream(getCacheDataFile(), true));

				out.writeInt(sourceProtIndex);
				out.writeInt(destProtIndex);

			} finally {
				if (out != null) out.close();
			}

			try {
				f.get();
			} catch (ExecutionException e) {
				if (e.getCause() instanceof IOException)
					throw (IOException) e.getCause();
				e.printStackTrace();
			} catch (InterruptedException e) {}
		}
	}

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
}
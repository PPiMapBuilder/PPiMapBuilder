package tk.nomis_tech.ppimapbuilder.data.store.otholog;

import tk.nomis_tech.ppimapbuilder.data.protein.Protein;
import tk.nomis_tech.ppimapbuilder.data.store.Organism;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OrganismPairOrthologCache implements Serializable {

	private final String identifier;

	private transient OrthologProteinIndex orthologProteinIndex;

	private transient File cacheDataFile;

	protected OrganismPairOrthologCache(Organism organismA, Organism organismB) throws IOException {
		// Ortholog cache data file for this organism pair
		identifier = organismA.getAbbrName() + "-" + organismB.getAbbrName();

		// Ortholog cache orthologProteinIndex file for this organism pair
		orthologProteinIndex = new OrthologProteinIndex(identifier);
	}

	private File getCacheDataFile() throws IOException {
		if (cacheDataFile == null)
			cacheDataFile = new File(PMBStore.getInstance().getOrthologCacheManager().getOrthologCacheFolder(), identifier + ".dat");
		return cacheDataFile;
	}

	public synchronized void addOrthologGroup(Protein proteinA, Protein proteinB) throws IOException {
		if (getOrtholog(proteinA, proteinB.getOrganism()) == null) {
			//Add protein (or not if already exist) to protein index and get its index
			int sourceProtIndex = orthologProteinIndex.addProtein(proteinA);
			int destProtIndex = orthologProteinIndex.addProtein(proteinB);

			Future f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					orthologProteinIndex.save();
					return null;
				}
			});

			//Append prot index pair to file
			DataOutputStream out = null;

			try {
				out = new DataOutputStream(new FileOutputStream(getCacheDataFile(), true));

				out.writeInt(sourceProtIndex);
				out.writeInt(destProtIndex);

			} catch (IOException e) {
				throw e;
			} finally {
				if (out != null) out.close();
			}

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

	public synchronized Protein getOrtholog(Protein proteinA, Organism organismB) throws IOException {
		if (orthologProteinIndex == null) {
			orthologProteinIndex = new OrthologProteinIndex(identifier);
		}
		int sourceProtIndex = orthologProteinIndex.indexOfProtein(proteinA);

		if (sourceProtIndex < 0)
			return null;

		Protein destProt = null;

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
						destProt = orthologProteinIndex.getProtein(protBIndex);
					} else if (protBIndex == sourceProtIndex) {
						destProt = orthologProteinIndex.getProtein(protAIndex);
					}

					if (destProt != null && destProt.getOrganism().equals(organismB))
						break;
					else destProt = null;
				} catch (EOFException e) {
					EOF = true;
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) in.close();
		}

		return destProt;
	}
}
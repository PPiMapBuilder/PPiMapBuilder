package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache;

import ch.picard.ppimapbuilder.data.settings.PMBSettings;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Util class encapsulating cache files.
 */
class CacheFile implements Serializable {

	private final String name;
	private transient File file;

	public CacheFile(String name) {
		this.name = name;
	}

	public File getOrCreateFile() throws IOException {
		if (!getFile().exists())
			getFile().createNewFile();

		return file;
	}

	public File getFile() {
		File baseFolder = PMBSettings.getInstance().getOrthologCacheFolder();
		if (!baseFolder.exists())
			baseFolder.mkdirs();

		if (file == null)
			file = new File(baseFolder, name);
		return file;
	}

	public void clear() throws IOException {
		File file = getFile();
		if(file.exists())
			file.delete();
	}

	public boolean exists() {
		return getFile().exists();
	}
}

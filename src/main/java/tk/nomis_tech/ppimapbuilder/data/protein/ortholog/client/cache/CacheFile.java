package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache;

import tk.nomis_tech.ppimapbuilder.data.settings.PMBSettings;

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

	public File getFile() throws IOException {
		File baseFolder = PMBSettings.getInstance().getOrthologCacheFolder();
		if (!baseFolder.exists())
			baseFolder.mkdirs();

		if (file == null)
			file = new File(baseFolder, name);

		if (!file.exists())
			file.createNewFile();

		return file;
	}

	public void clear() throws IOException {
		File file = getFile();
		if(file.exists())
			file.delete();
	}

	public boolean exists() {
		return file != null && file.exists();
	}
}

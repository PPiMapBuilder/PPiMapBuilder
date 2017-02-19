/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
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

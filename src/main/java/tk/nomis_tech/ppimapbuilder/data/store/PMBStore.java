package tk.nomis_tech.ppimapbuilder.data.store;

import tk.nomis_tech.ppimapbuilder.data.store.otholog.OrthologCacheManager;
import tk.nomis_tech.ppimapbuilder.data.store.settings.PMBSettings;

import java.io.File;
import java.io.IOException;

public class PMBStore {

	private static PMBStore _instance;

	// Cytoscape configuration folder
	private static File cytoscapeConfigurationFolder;

	// PPiMapBuilder configuration folder
	private static File ppiMapBuilderConfigurationFolder;

	// Create PPiMapBuilder configuration folder in file system if it doesn't exist
	private PMBStore() {
		cytoscapeConfigurationFolder = new File(System.getProperty("user.home"), "CytoscapeConfiguration");
		ppiMapBuilderConfigurationFolder = new File(cytoscapeConfigurationFolder, "PPiMapBuilder");

		if (!ppiMapBuilderConfigurationFolder.exists())
			ppiMapBuilderConfigurationFolder.mkdir();
	}

	public static PMBStore getInstance() {
		if(_instance == null)
			_instance = new PMBStore();
		return _instance;
	}

	public static File getPpiMapBuilderConfigurationFolder() {
		return ppiMapBuilderConfigurationFolder;
	}

	// PPiMapBuilder settings
	private PMBSettings settings;

	public PMBSettings getSettings() {
		if (settings == null)
			settings = new PMBSettings();
		return settings;
	}

	// PPiMapBuilder store cache
	private OrthologCacheManager orthologCacheManager;

	public OrthologCacheManager getOrthologCacheManager() throws IOException {
		if(orthologCacheManager == null)
			orthologCacheManager = new OrthologCacheManager();
		return orthologCacheManager;
	}
}

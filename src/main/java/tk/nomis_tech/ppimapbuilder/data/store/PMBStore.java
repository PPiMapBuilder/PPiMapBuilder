package tk.nomis_tech.ppimapbuilder.data.store;

import java.io.File;

public class PMBStore {

	// Cytoscape configuration folder
	private static File cytoscapeConfigurationFolder = new File(System.getProperty("user.home"), "CytoscapeConfiguration");

	// PPiMapBuilder configuration folder
	private static File ppiMapBuilderConfigurationFolder = new File(cytoscapeConfigurationFolder, "PPiMapBuilder");

	// Create PPiMapBuilder configuration folder in file system if it doesn't exist
	{
		if (!ppiMapBuilderConfigurationFolder.exists())
			ppiMapBuilderConfigurationFolder.mkdir();
	}

	public static File getPpiMapBuilderConfigurationFolder() {
		return ppiMapBuilderConfigurationFolder;
	}

	// PPiMapBuilder settings
	private static PMBSettings settings;

	public static PMBSettings getSettings() {
		if (settings == null)
			settings = new PMBSettings();
		return settings;
	}

	// PPiMapBuilder store cache
	private static OrthologCache orthologCache;

	public static OrthologCache getOrthologCache() {
		if(orthologCache == null)
			orthologCache = new OrthologCache();
		return orthologCache;
	}
}

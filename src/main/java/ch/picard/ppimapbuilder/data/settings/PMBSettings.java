package ch.picard.ppimapbuilder.data.settings;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologySet;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PMBSettings {

	private File orthologCacheFolder;
	private List<String> databaseList;
	private List<Organism> organismList;
	private List<GeneOntologySet> goSlimList;

	private PMBSettings(
			List<String> databaseList,
			List<Organism> organismList,
			List<GeneOntologySet> goSlimList
	) {
		this.orthologCacheFolder = new File(pmbSettingsHandler.ppiMapBuilderConfigurationFolder, "ortholog-cache");
		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdir();

		this.databaseList = databaseList == null ?
				// Default database list
				new ArrayList<String>(Arrays.asList(
						"BioGrid",
						"DIP",
						"IntAct",
						"MINT"
				)) :
				// Existing database list
				databaseList;

		this.organismList = organismList == null ?
				// Default organism list
				UserOrganismRepository.getDefaultOrganismList() :
				// Existing organism list
				organismList;

		this.goSlimList = goSlimList == null ?
				// Default GO slim list
				Arrays.asList(GeneOntologySet.getDefaultGOslim()) :
				// Existing GO slim list
				goSlimList;
	}

	public List<String> getDatabaseList() {
		return databaseList;
	}

	public void setDatabaseList(List<String> databaseList) {
		this.databaseList = databaseList;
	}

	public List<Organism> getOrganismList() {
		return organismList;
	}

	public void setOrganismList(List<Organism> organismList) {
		this.organismList = organismList;
	}

	public File getOrthologCacheFolder() {
		return orthologCacheFolder;
	}

	//For test purpose only
	public void setOrthologCacheFolder(File orthologCacheFolder) throws IOException {
		this.orthologCacheFolder = orthologCacheFolder;
	}

	public List<GeneOntologySet> getGoSlimList() {
		return goSlimList;
	}

	public void setGoSlimList(List<GeneOntologySet> goSlimList) {
		this.goSlimList = goSlimList;
	}

	private static final PMBSettingsHandler pmbSettingsHandler;

	static {
		File cytoscapeConfigurationFolder = new File(System.getProperty("user.home"), "CytoscapeConfiguration");
		pmbSettingsHandler = new PMBSettingsHandler(
				new File(System.getProperty("user.home"), "CytoscapeConfiguration"),
				new File(cytoscapeConfigurationFolder, "PPiMapBuilder")
		);
	}

	private static PMBSettings _instance;

	public static PMBSettings getInstance() {
		if (_instance == null)
			load();
		return _instance;
	}

	public static void save() {
		pmbSettingsHandler.saveSettings(_instance);
	}

	public static void load() {
		_instance = pmbSettingsHandler.loadSettings();
	}

	private static class PMBSettingsHandler {

		private final File cytoscapeConfigurationFolder;
		private final File ppiMapBuilderConfigurationFolder;
		private final File pmbSettingFile;

		private PMBSettingsHandler(File cytoscapeConfigurationFolder, File ppiMapBuilderConfigurationFolder) {
			this.cytoscapeConfigurationFolder = cytoscapeConfigurationFolder;
			this.ppiMapBuilderConfigurationFolder = ppiMapBuilderConfigurationFolder;
			this.pmbSettingFile = new File(ppiMapBuilderConfigurationFolder, "ppimapbuilder.settings");
		}

		PMBSettings saveSettings(PMBSettings settings) {
			ObjectOutputStream fileOut = null;
			try {
				fileOut = new ObjectOutputStream(new FileOutputStream(pmbSettingFile));
				fileOut.writeObject(settings.databaseList);
				fileOut.writeObject(settings.organismList);
				fileOut.writeObject(settings.goSlimList);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fileOut != null)
					try {
						fileOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				return settings;
			}
		}

		PMBSettings newSettings() {
			return new PMBSettings(
					null,
					null,
					null
			);
		}

		PMBSettings loadSettings() {
			if (!ppiMapBuilderConfigurationFolder.exists()) {
				ppiMapBuilderConfigurationFolder.mkdir();
			} else {
				if (!pmbSettingFile.exists()) {
					File pmbDatabaseSettingFile = new File(ppiMapBuilderConfigurationFolder, "ppimapbuilder-databases.settings");
					File pmbOrganismSettingFile = new File(ppiMapBuilderConfigurationFolder, "ppimapbuilder-organisms.settings");

					if (pmbDatabaseSettingFile.exists() && pmbOrganismSettingFile.exists()) {
						final PMBSettings settings = loadV09Settings(pmbDatabaseSettingFile, pmbOrganismSettingFile);
						pmbDatabaseSettingFile.delete();
						pmbOrganismSettingFile.delete();
						return saveSettings(settings);
					}
				} else
					return loadV010Settings();
			}
			return saveSettings(newSettings());
		}

		PMBSettings loadV010Settings() {
			List<String> databaseList = null;
			List<Organism> organismList = null;
			List<GeneOntologySet> goSlimList = null;

			ObjectInputStream fileIn = null;
			try {
				fileIn = new ObjectInputStream(new FileInputStream(pmbSettingFile));
				databaseList = (List<String>) fileIn.readObject();
				organismList = (List<Organism>) fileIn.readObject();
				goSlimList = (List<GeneOntologySet>) fileIn.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				return saveSettings(newSettings());
			} finally {
				if (fileIn != null)
					try {
						fileIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			return new PMBSettings(
					databaseList,
					organismList,
					goSlimList
			);
		}

		PMBSettings loadV09Settings(File pmbDatabaseSettingFile, File pmbOrganismSettingFile) {
			ObjectInputStream fileIn = null;
			ArrayList<String> databaseList = null;
			try {
				fileIn = new ObjectInputStream(new FileInputStream(pmbDatabaseSettingFile));
				databaseList = (ArrayList<String>) fileIn.readObject();
			} catch (Exception e) {
				databaseList = null;
			} finally {
				if (fileIn != null)
					try {
						fileIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			fileIn = null;

			ArrayList<Organism> organismList = null;
			try {
				fileIn = new ObjectInputStream(new FileInputStream(pmbOrganismSettingFile));
				organismList = (ArrayList<Organism>) fileIn.readObject();
			} catch (Exception e) {
				organismList = null;
			} finally {
				if (fileIn != null)
					try {
						fileIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return new PMBSettings(
					databaseList,
					organismList,
					null
			);
		}
	}
}

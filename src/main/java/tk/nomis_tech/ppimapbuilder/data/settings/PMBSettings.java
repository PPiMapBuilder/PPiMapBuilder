package tk.nomis_tech.ppimapbuilder.data.settings;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.UserOrganismRepository;

public final class PMBSettings {

	private static PMBSettings _instance;

	// PPiMapBuilder setting file
	private final File pmbDatabaseSettingFile;
	private ArrayList<String> databaseList;
	
	private final File pmbOrganismSettingFile;
	private ArrayList<Organism> organismList;

	// Cytoscape configuration folder
	private final File cytoscapeConfigurationFolder;

	// PPiMapBuilder configuration folder
	private final File ppiMapBuilderConfigurationFolder;

	private File orthologCacheFolder;

	private PMBSettings() {
		

		System.out.println("#4.1");
		
		cytoscapeConfigurationFolder = new File(System.getProperty("user.home"), "CytoscapeConfiguration");
		ppiMapBuilderConfigurationFolder = new File(cytoscapeConfigurationFolder, "PPiMapBuilder");


		System.out.println("#4.2");
		
		if (!ppiMapBuilderConfigurationFolder.exists())
			ppiMapBuilderConfigurationFolder.mkdir();

		orthologCacheFolder = new File(ppiMapBuilderConfigurationFolder, "ortholog-cache");

		if (!orthologCacheFolder.exists())
			orthologCacheFolder.mkdir();

		

		System.out.println("#4.3");
		// Default database list
		databaseList = new ArrayList<String>(Arrays.asList(
				"BioGrid",
				"DIP",
				"IntAct",
				"MINT"
		));

		pmbDatabaseSettingFile = new File(ppiMapBuilderConfigurationFolder, "ppimapbuilder-databases.settings");
		

		System.out.println("#4.4");
		// Default organism list
		
		//System.out.println(UserOrganismRepository.getDefaultOrganismList());
		
		organismList = UserOrganismRepository.getDefaultOrganismList();
		
		System.out.println("#4.45");System.out.println(organismList);
		
		pmbOrganismSettingFile = new File(ppiMapBuilderConfigurationFolder, "ppimapbuilder-organisms.settings");


		System.out.println("#4.5");
		
		readSettings();
	}

	public static PMBSettings getInstance() {

		if (_instance == null) {
			_instance = new PMBSettings();
		}
		return _instance;
	}

	public ArrayList<String> getDatabaseList() {
		return databaseList;
	}

	public void setDatabaseList(ArrayList<String> databaseList) {
		this.databaseList = databaseList;
	}
	
	public ArrayList<Organism> getOrganismList() {

		System.out.println("#4.9.2");
		return organismList;
	}

	public void setOrganismList(ArrayList<Organism> organismList) {
		this.organismList = organismList;
	}

	public void writeSettings() {
		try {
			FileOutputStream fileOut = new FileOutputStream(pmbDatabaseSettingFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(databaseList);
			out.close();
			fileOut.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(pmbOrganismSettingFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(organismList);
			out.close();
			fileOut.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readSettings() {

		System.out.println("#4.6");
		try {
			FileInputStream fileIn = new FileInputStream(pmbDatabaseSettingFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			setDatabaseList((ArrayList<String>) in.readObject());
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// If the setting save file does not exist, we the default database list
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		System.out.println("#4.7");
		try {
			FileInputStream fileIn = new FileInputStream(pmbOrganismSettingFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			setOrganismList((ArrayList<Organism>) in.readObject());
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// If the setting save file does not exist, we the default organism list
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("#4.8");
	}

	public File getOrthologCacheFolder() {
		return orthologCacheFolder;
	}

	//For test purpose only
	public void setOrthologCacheFolder(File orthologCacheFolder) throws IOException {
		this.orthologCacheFolder = orthologCacheFolder;
	}
}

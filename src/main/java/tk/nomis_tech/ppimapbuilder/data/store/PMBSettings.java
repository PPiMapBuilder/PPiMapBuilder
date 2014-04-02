package tk.nomis_tech.ppimapbuilder.data.store;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public final class PMBSettings {

	private static PMBSettings _instance;

	// PPiMapBuilder setting file
	private final File pmbSettingFile;

	protected PMBSettings() {
		// Default database list
		databaseList = new ArrayList<String>(Arrays.asList(
				"BioGrid",
				"DIP",
				"IntAct",
				"MINT"
		));

		pmbSettingFile = new File(PMBStore.getPpiMapBuilderConfigurationFolder(), "ppimapbuilder.settings");

		readSettings();
	}

	private ArrayList<String> databaseList;

	public ArrayList<String> getDatabaseList() {
		return databaseList;
	}

	public void setDatabaseList(ArrayList<String> databaseList) {
		this.databaseList = databaseList;
	}

	public void writeSettings() {
		try {
			FileOutputStream fileOut = new FileOutputStream(pmbSettingFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(databaseList);
			out.close();
			fileOut.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readSettings() {
		try {
			FileInputStream fileIn = new FileInputStream(pmbSettingFile);
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
	}

}

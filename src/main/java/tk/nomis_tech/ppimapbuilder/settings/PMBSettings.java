package tk.nomis_tech.ppimapbuilder.settings;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class PMBSettings {
	
	private static final long serialVersionUID = 1L;
	
	private static ArrayList<String> databaseList = new ArrayList<String>();
	
	public static ArrayList<String> getDatabaseList() {
		return databaseList;
	}
	
	public static void setDatabaseList(ArrayList<String> databaseList) {
		PMBSettings.databaseList = databaseList;
	}
	
	public static void writeSettings() {
		try {
            FileOutputStream fileOut = new FileOutputStream("/home/pidupuis/CytoscapeConfiguration/PPiMapBuilder/"+"settings.sav");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(PMBSettings.getDatabaseList());
            out.close();
            fileOut.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

}

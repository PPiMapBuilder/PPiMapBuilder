package tk.nomis_tech.ppimapbuilder.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
            FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home")+"/CytoscapeConfiguration/PPiMapBuilder/"+"settings.sav");
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
	
	public static void readSettings() {
		try {
            FileInputStream fileIn = new FileInputStream(System.getProperty("user.home")+"/CytoscapeConfiguration/PPiMapBuilder/"+"settings.sav");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            PMBSettings.setDatabaseList((ArrayList<String>) in.readObject());
            in.close();
            fileIn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
        	// If the setting save file does not exist, we add some default values
        	ArrayList<String> databases = new ArrayList<String>();
        	databases.add("BioGrid");
        	databases.add("DIP");
        	databases.add("IntAct");
        	databases.add("MINT");
        	PMBSettings.setDatabaseList(databases);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

}

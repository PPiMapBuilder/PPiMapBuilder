package tk.nomis_tech.ppimapbuilder.settings;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBSettingSaveTask extends AbstractTask {
	
	public PMBSettingSaveTask() {
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		PMBSettings.writeSettings();
	}

}

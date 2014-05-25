package ch.picard.ppimapbuilder.data.settings;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBSettingSaveTask extends AbstractTask {
	
	public PMBSettingSaveTask() {
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		PMBSettings.getInstance().writeSettings();
	}

}

package tk.nomis_tech.ppimapbuilder.data.store;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.store.PMBStore;

public class PMBSettingSaveTask extends AbstractTask {
	
	public PMBSettingSaveTask() {
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		PMBStore.getSettings().writeSettings();
	}

}

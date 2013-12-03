package tk.nomis_tech.ppimapbuilder;

import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;
import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class PMBMenu extends AbstractTask {

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				(new QueryWindow()).setVisible(true);
			}
		});
	}

}

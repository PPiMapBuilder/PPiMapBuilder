package tk.nomis_tech.ppimapbuilder;

import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import tk.nomis_tech.ppimapbuilder.ui.QueryWindow;

/**
 * The interaction query menu
 */
public class PMBQueryMenu extends AbstractTask {

    @Override
    public void run(TaskMonitor arg0) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                (new QueryWindow()).setVisible(true);
            }
        });
    }

}

package ch.picard.ppimapbuilder;

import javax.swing.SwingUtilities;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import ch.picard.ppimapbuilder.ui.credits.CreditFrame;

/**
 * The interaction query menu
 */
public class PMBCreditMenuTask extends AbstractTask {

	private CreditFrame creditWindow;

	public PMBCreditMenuTask(CreditFrame creditWindow) {
		this.creditWindow = creditWindow;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				creditWindow.setVisible(true);
			}
		});
	}

}

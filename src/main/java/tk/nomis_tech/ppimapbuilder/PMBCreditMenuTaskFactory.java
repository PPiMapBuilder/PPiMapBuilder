package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import tk.nomis_tech.ppimapbuilder.ui.credits.CreditFrame;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBCreditMenuTaskFactory extends AbstractTaskFactory {

	private CreditFrame creditWindow;

	public PMBCreditMenuTaskFactory(CreditFrame creditWindow) {
		this.creditWindow = creditWindow;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new PMBCreditMenuTask(creditWindow)
		);
	}

}

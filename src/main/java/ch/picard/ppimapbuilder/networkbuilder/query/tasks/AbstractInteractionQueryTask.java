package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.data.client.ThreadedClientManager;
import org.cytoscape.work.Task;

public abstract class AbstractInteractionQueryTask implements Task {

	protected final ThreadedClientManager threadedClientManager;

	public AbstractInteractionQueryTask(ThreadedClientManager threadedClientManager) {
		this.threadedClientManager = threadedClientManager;
	}

	@Override
	public void cancel() {
		threadedClientManager.shutdown();
	}

}

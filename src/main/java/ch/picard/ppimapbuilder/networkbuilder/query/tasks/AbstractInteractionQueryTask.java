package ch.picard.ppimapbuilder.networkbuilder.query.tasks;

import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.Task;

public abstract class AbstractInteractionQueryTask implements Task {

	protected final ExecutorServiceManager executorServiceManager;

	public AbstractInteractionQueryTask(ExecutorServiceManager executorServiceManager) {
		this.executorServiceManager = executorServiceManager;
	}

	@Override
	public void cancel() {
		executorServiceManager.shutdown();
	}

}

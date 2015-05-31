package ch.picard.ppimapbuilder.util.task;

import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.cytoscape.work.Task;

public abstract class AbstractThreadedTask implements Task {

	private boolean canceled = false;
	protected final ExecutorServiceManager executorServiceManager;

	public AbstractThreadedTask(ExecutorServiceManager executorServiceManager) {
		this.executorServiceManager = executorServiceManager;
	}

	@Override
	public void cancel() {
		executorServiceManager.shutdown();
		canceled = true;
	}

	public boolean isCanceled() {
		return canceled;
	}
}

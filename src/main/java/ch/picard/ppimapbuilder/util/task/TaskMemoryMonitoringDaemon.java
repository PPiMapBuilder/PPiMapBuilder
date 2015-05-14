package ch.picard.ppimapbuilder.util.task;

import javax.swing.*;

public class TaskMemoryMonitoringDaemon extends Thread {

	private final AbstractThreadedTask task;
	private final Runtime runtime;

	public TaskMemoryMonitoringDaemon(AbstractThreadedTask task) {
		this.task = task;
		setDaemon(true);
		runtime = Runtime.getRuntime();
	}

	private double getPercentMemUsed() {
		final double RAM_TOTAL = runtime.totalMemory();
		final double RAM_FREE = runtime.freeMemory();
		final double RAM_USED = RAM_TOTAL - RAM_FREE;
		return RAM_USED / RAM_TOTAL;
	}

	@Override
	public void run() {
		double averagePercentMemUsed = getPercentMemUsed();

		while (!task.isCanceled()) {
			try {
				averagePercentMemUsed = (averagePercentMemUsed + getPercentMemUsed()) / 2d;
				if (averagePercentMemUsed > 0.822) {
					task.cancel();
					JOptionPane.showMessageDialog(null,
							"Cytoscape ran out of memory.\n" +
							"Please consider adding memory to Cytoscape by editing the 'cytoscape.vmoptions' file",
							"Out of memory error",
							JOptionPane.ERROR_MESSAGE
					);
				}
				Thread.sleep(500);
			} catch (Exception e) {
				break;
			}
		}
	}
}

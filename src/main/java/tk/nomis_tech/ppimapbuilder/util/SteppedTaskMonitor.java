package tk.nomis_tech.ppimapbuilder.util;

import org.cytoscape.work.TaskMonitor;

/**
 * Simple Task implementation that tracks progress using a fixed number of steps
 */
public class SteppedTaskMonitor implements TaskMonitor {

	private final double nbStep;
	private int currentStep;
	private TaskMonitor monitor;


	public SteppedTaskMonitor(TaskMonitor monitor, final double nbStep) {
		this.monitor = monitor;
		this.nbStep = nbStep;
		this.currentStep = -1;
	}

	public void setStep(String message) {
		setStatusMessage(message);
		setProgress(++currentStep / nbStep);
	}

	@Override
	public void setTitle(String s) {
		if (monitor != null) {
			monitor.setTitle(s);
		}
		System.out.println("[TITLE]\t\t" + s);

	}

	@Override
	public void setProgress(double v) {
		if (monitor != null) {
			monitor.setProgress(v);
		}
		System.out.println("[PROGRESS]\t" + (int) (v * 100.0) + "%");
	}

	@Override
	public void setStatusMessage(String s) {
		if (monitor != null) {
			monitor.setStatusMessage(s);
		}
		System.out.println("[STATUS]\t" + s);

	}
}

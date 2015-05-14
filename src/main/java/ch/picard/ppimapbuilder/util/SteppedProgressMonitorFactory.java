package ch.picard.ppimapbuilder.util;

import java.util.HashMap;

public class SteppedProgressMonitorFactory {

	private final ProgressMonitor progressMonitor;
	private final double nbStep;
	private final double[] totalProgress;
	private final HashMap<Integer, Double> progressByStep;

	public SteppedProgressMonitorFactory(ProgressMonitor progressMonitor, int nbStep) {
		this.progressMonitor = progressMonitor;
		this.nbStep = nbStep;
		this.totalProgress = new double[]{0d};
		this.progressByStep = new HashMap<Integer, Double>();
		for(int i = 0; i < nbStep; i++) {
			progressByStep.put(i, 0d);
		}
	}

	public ProgressMonitor createStepProgressMonitor(final int step) {
		return new ProgressMonitor() {
			@Override
			public void setProgress(double v) {
				synchronized(this) {
					totalProgress[0] -= progressByStep.get(step);
					progressByStep.put(step, v);
					progressMonitor.setProgress(
							(totalProgress[0] += v) / nbStep
					);
				}
			}
		};
	}
}

package ch.picard.ppimapbuilder.util;

import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class merging ProgressMonitor and TaskMonitor in order to bring type compatibility between them.
 */
public class ProgressTaskMonitor implements ProgressMonitor, TaskMonitor {

	private final TaskMonitor taskMonitor;
	private final ProgressMonitor progressMonitor;

	private ProgressTaskMonitor(TaskMonitor taskMonitor, ProgressMonitor progressMonitor) {
		this.taskMonitor = taskMonitor;
		this.progressMonitor = progressMonitor;
	}

	/**
	 * Create an inert ProgressTaskMonitor.
	 */
	public ProgressTaskMonitor() {
		this(null, null);
	}

	/**
	 * Enables a TaskMonitor to be used as a ProgressMonitor
	 */
	public ProgressTaskMonitor(TaskMonitor taskMonitor) {
		this(taskMonitor, null);
	}

	/**
	 * Enables a ProgressMonitor to be used as a TaskMonitor.
	 * Titles and status message will be ignored.
	 */
	public ProgressTaskMonitor(ProgressMonitor progressMonitor) {
		this(null, progressMonitor);
	}

	@Override
	public void setTitle(String s) {
		if(taskMonitor != null) taskMonitor.setTitle(s);
	}

	@Override
	public void setStatusMessage(String s) {
		if(taskMonitor != null) taskMonitor.setStatusMessage(s);
	}

	@Override
	public void setProgress(double v) {
		if(taskMonitor != null) taskMonitor.setProgress(v);
		else if(progressMonitor != null) progressMonitor.setProgress(v);
	}

	/**
	 * Splits a ProgressMonitor into n monitors that can be used for subtasks
	 */
	public List<ProgressTaskMonitor> split(final Integer n) {
		List<ProgressTaskMonitor> monitors = new ArrayList<ProgressTaskMonitor>();
		for(int i = 1; i <= n; i++) {
			monitors.add(new ProgressTaskMonitor(){

				@Override
				public void setStatusMessage(String s) {
					ProgressTaskMonitor.this.setStatusMessage(s);
				}

				@Override
				public void setTitle(String s) {
					ProgressTaskMonitor.this.setTitle(s);
				}

				@Override
				public void setProgress(double v) {
					ProgressTaskMonitor.this.setProgress(v/(double)n);
				}
			});
		}
		return monitors;
	}
}

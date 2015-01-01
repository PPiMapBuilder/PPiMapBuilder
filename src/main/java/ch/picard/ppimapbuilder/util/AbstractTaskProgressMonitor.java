package ch.picard.ppimapbuilder.util;

import org.cytoscape.work.TaskMonitor;

public abstract class AbstractTaskProgressMonitor implements ProgressMonitor, TaskMonitor {
	@Override
	public void setTitle(String s) {}

	@Override
	public void setStatusMessage(String s) {}
}

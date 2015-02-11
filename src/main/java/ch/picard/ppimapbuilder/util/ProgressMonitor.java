package ch.picard.ppimapbuilder.util;

/**
 * A simple class used to report progress of a long task.
 * @see ch.picard.ppimapbuilder.util.ProgressTaskMonitor
 */
public interface ProgressMonitor {
	public void setProgress(double v);
}

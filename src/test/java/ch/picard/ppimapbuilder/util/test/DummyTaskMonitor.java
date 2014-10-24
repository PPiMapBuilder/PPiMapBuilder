package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.work.TaskMonitor;

public class DummyTaskMonitor implements TaskMonitor {

	@Override
	public void setTitle(String s) {
		System.out.println("[TITLE]\t\t" + s);
	}

	@Override
	public void setProgress(double v) {
		System.out.println("[PROGRESS]\t" + (int) (v * 100.0) + "%");
	}

	@Override
	public void setStatusMessage(String s) {
		System.out.println("[STATUS]\t" + s);
	}

}

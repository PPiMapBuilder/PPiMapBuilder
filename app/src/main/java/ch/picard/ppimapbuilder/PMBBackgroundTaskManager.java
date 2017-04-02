package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.ui.resultpanel.BackgroundTaskMonitor;
import ch.picard.ppimapbuilder.ui.resultpanel.ResultPanel;
import org.cytoscape.work.Task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PMBBackgroundTaskManager {

	private final ResultPanel resultPanel;

	public PMBBackgroundTaskManager(ResultPanel resultPanel) {
		this.resultPanel = resultPanel;
	}

	public void launchTask(final Task task) {
		new Thread() {
			@Override
			public void run() {
				try {
					BackgroundTaskMonitor backgroundTaskMonitor =
							new BackgroundTaskMonitor(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									task.cancel();
								}
							});
					resultPanel.setBackgroundTask(backgroundTaskMonitor);
					task.run(backgroundTaskMonitor);
				} catch (Exception ignored) {
				}
				resultPanel.setBackgroundTask(null);
			}
		}.start();
	}

}

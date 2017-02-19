package ch.picard.ppimapbuilder.ui.resultpanel;

import ch.picard.ppimapbuilder.ui.util.field.DeleteButton;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import java.awt.event.ActionListener;

public class BackgroundTaskMonitor extends JPanel implements TaskMonitor {

	private final JProgressBar taskProgressBar;
	private final JLabel taskTitleLabel;
	private final int[] currentProgress;

	public BackgroundTaskMonitor(ActionListener cancelListener) {
		setLayout(new MigLayout("ins 5", "[grow][]", "[20px][20px]"));

		taskTitleLabel = new JLabel();
		add(taskTitleLabel, "grow, spanx 2, wrap");

		taskProgressBar = new JProgressBar();
		taskProgressBar.setIndeterminate(true);
		taskProgressBar.setMinimum(0);
		taskProgressBar.setMaximum(100);
		add(taskProgressBar, "grow");

		JButton cancel = new DeleteButton();
		cancel.addActionListener(cancelListener);
		add(cancel);

		currentProgress = new int[]{0};
	}

	@Override
	public void setTitle(final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				taskTitleLabel.setText(s);
				repaint();
			}
		});
	}

	@Override
	public void setProgress(final double percent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int progress = (int) (percent * 100.0);

				if(progress > currentProgress[0]) {
					if(taskProgressBar.isIndeterminate()) {
						taskProgressBar.setIndeterminate(false);
					}
					taskProgressBar.setValue(currentProgress[0] = progress);
					repaint();
				}
			}
		});
	}

	@Override
	public void setStatusMessage(final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				taskTitleLabel.setText(s);
				repaint();
			}
		});
	}
}

package ch.picard.ppimapbuilder.util.concurrent;

import ch.picard.ppimapbuilder.util.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ConcurrentListExecutor {

	/**
	 * Run ListRequest concurrently
	 */
	public static <T> List<T> getResults(
			final List<? extends ListRequest<T>> requests,
			final ExecutorServiceManager executorServiceManager,
			final ProgressMonitor progressMonitor
	) {
		final List<T> mergedResult = new ArrayList<T>();
		final double size = requests.size();
		final double[] state = new double[]{0d, 0d}; // index, progress
		new ConcurrentExecutor<List<T>>(executorServiceManager, requests.size()) {
			@Override
			public Callable<List<T>> submitRequests(int index) {
				return requests.get(index);
			}

			@Override
			public void processResult(List<T> result, Integer index) {
				final double percent = state[0]++ / size;
				if (progressMonitor != null && percent > state[1])
					progressMonitor.setProgress(state[1] = percent);
				mergedResult.addAll(result);
			}
		}.run();
		return mergedResult;
	}

	public static <T> List<T> getResults(
			final List<? extends ListRequest<T>> requests,
			final ExecutorServiceManager executorServiceManager
	) {
		return getResults(requests, executorServiceManager, null);
	}

}

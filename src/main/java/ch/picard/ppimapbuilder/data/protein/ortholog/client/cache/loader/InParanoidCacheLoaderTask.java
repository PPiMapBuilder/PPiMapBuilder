package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;
import ch.picard.ppimapbuilder.util.ClassLoaderHack;
import ch.picard.ppimapbuilder.util.SteppedTaskMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ConcurrentExecutor;
import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class InParanoidCacheLoaderTask extends AbstractTask {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/Orthologs_OrthoXML/";
	private final InParanoidCacheLoaderTaskFactory parent;
	private final CloseableHttpClient httpClient;
	private final ExecutorService executorService;
	private final Task callback;
	private boolean cancelled = false;

	public InParanoidCacheLoaderTask(InParanoidCacheLoaderTaskFactory parent, Task callback) {
		this.callback = callback;
		this.httpClient = HttpClientBuilder.create().build();
		this.parent = parent;
		this.executorService = Executors.newFixedThreadPool(5);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final List<Pair<Organism>> organismCombination = new ArrayList<Pair<Organism>>(OrganismUtils.createCombinations(parent.getOrganisms()));

		//Set number of steps
		final SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size() + 1);
		monitor.setTitle("Loading InParanoid OrthoXML files in cache");

		final int[] i = new int[]{0};

		monitor.setStep("Preparing requests...");
		new ConcurrentExecutor<CacheLoadRequest>(executorService, organismCombination.size()) {
			@Override
			public Callable<CacheLoadRequest> submitRequests(int index) {
				Pair<Organism> organismCouple = organismCombination.get(index);
				Organism organismA = organismCouple.getFirst();
				Organism organismB = organismCouple.getSecond();

				try {
					SpeciesPairProteinOrthologCache cache =
							PMBProteinOrthologCacheClient.getInstance()
								.getSpeciesPairProteinOrthologCache(organismA, organismB);

					if (!cache.isFull()) {
						return new CacheLoadRequest(
								organismA,
								organismB,

								BASE_URL
										+ organismA.getAbbrName()
										+ "/"
										+ organismA.getAbbrName() + "-" + organismB.getAbbrName() + ".orthoXML",

								cache
						);
					}
				} catch (Exception ignored){}

				return null;
			}

			@Override
			public void processResult(CacheLoadRequest intermediaryResult, Integer index) {
				monitor.setStep(
						"Loaded: " +
								intermediaryResult.organismA.getSimpleScientificName() +
								" - " +
								intermediaryResult.organismB.getSimpleScientificName()
				);
				i[0]++;
			}
		}.run();

		if (i[0] == 0) {
			parent.setMessage("Orthology cache fully loaded from InParanoid");
			return;
		}

		monitor.setProgress(1);

		PMBProteinOrthologCacheClient.getInstance().save();

		if (callback != null) insertTasksAfterCurrentTask(callback);
	}

	@Override
	public void cancel() {
		cancelled = true;
		executorService.shutdownNow();
		try {
			parent.getCallback().run(null);
		} catch (Exception e) {}
	}

	class CacheLoadRequest implements Callable<CacheLoadRequest> {
		private final Organism organismA;
		private final Organism organismB;
		private final String url;
		private final SpeciesPairProteinOrthologCache cache;

		CacheLoadRequest(Organism organismA, Organism organismB, String url, SpeciesPairProteinOrthologCache cache) {
			this.organismA = organismA;
			this.organismB = organismB;
			this.url = url;
			this.cache = cache;
		}

		@Override
		public CacheLoadRequest call() throws Exception {
			if (!InParanoidCacheLoaderTask.this.cancelled) {
				HttpRequestBase req = null;
				CloseableHttpResponse res = null;

				try {
					req = new HttpGet(url.toString());
					res = httpClient.execute(req);

					int statusCode = res.getStatusLine().getStatusCode();

					if (200 <= statusCode && statusCode < 300) {

						final InputStream input = res.getEntity().getContent();

						if (!cache.isFull()) {
							ClassLoaderHack.runWithHack(new ClassLoaderHack.ThrowingRunnable() {
								@Override
								public void run() throws Exception {
									new OrthoXMLParser(input, cache).parse();
								}
							}, WstxInputFactory.class);
						}
					}
				} finally {
					if (res != null) res.close();
					if (req != null) req.releaseConnection();
				}
			}
			return this;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		httpClient.close();
	}

}
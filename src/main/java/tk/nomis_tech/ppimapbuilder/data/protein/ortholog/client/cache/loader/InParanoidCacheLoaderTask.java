package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismUtils;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;
import tk.nomis_tech.ppimapbuilder.task.SteppedTaskMonitor;
import tk.nomis_tech.ppimapbuilder.util.ClassLoaderHack;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

class InParanoidCacheLoaderTask implements Task {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/OrthoXML/";
	private final InParanoidCacheLoaderTaskFactory parent;
	private final CloseableHttpClient httpClient;
	private final ExecutorService executorService;
	private boolean cancelled = false;

	public InParanoidCacheLoaderTask(InParanoidCacheLoaderTaskFactory parent) {
		this.httpClient = HttpClientBuilder.create().build();
		this.parent = parent;
		this.executorService = Executors.newFixedThreadPool(3);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Set<Pair<Organism>> organismCombination = OrganismUtils.createCombinations(parent.getOrganisms());

		//Set number of steps
		SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size() + 1);
		monitor.setTitle("Loading InParanoid OrthoXML files in cache");

		List<Future<CacheLoadRequest>> requests = new ArrayList<Future<CacheLoadRequest>>();
		CompletionService<CacheLoadRequest> completionService = new ExecutorCompletionService<CacheLoadRequest>(executorService);

		monitor.setStep("Preparing requests...");
		for (Pair<Organism> organismCouple : organismCombination) {
			Organism organismA = organismCouple.getFirst();
			Organism organismB = organismCouple.getSecond();

			CacheLoadRequest request = new CacheLoadRequest(
					organismA,
					organismB,

					new StringBuilder(BASE_URL)
							.append(organismA.getAbbrName()).append("/")
							.append(organismA.getAbbrName())
							.append("-")
							.append(organismB.getAbbrName())
							.append(".orthoXML")
							.toString(),

					PMBProteinOrthologCacheClient.getInstance()
							.getSpeciesPairProteinOrthologCache(organismA, organismB),

					this
			);
			requests.add(completionService.submit(request));

			if (cancelled)
				break;
		}

		Future<CacheLoadRequest> future;
		for (int i = 0, requestsSize = requests.size(); i < requestsSize; i++) {
			try {
				if(cancelled || executorService.isShutdown() || executorService.isTerminated())
					break;

				future = completionService.take();

				CacheLoadRequest cacheLoadRequest = future.get();

				if (!cacheLoadRequest.canceled) {
					monitor.setStep(
							(cacheLoadRequest.skipped ? "Already loaded: " : "Loaded: ")
									+ cacheLoadRequest.organismA.getSimpleScientificName()
									+ " - "
									+ cacheLoadRequest.organismB.getSimpleScientificName()
					);
				}
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				if(!(e.getCause() instanceof InterruptedException))
					e.printStackTrace();
			}
		}

		monitor.setProgress(1);
		if (parent.getListener() != null)
			parent.getListener().done();
	}

	@Override
	public void cancel() {
		cancelled = true;
		executorService.shutdownNow();
	}

	class CacheLoadRequest implements Callable<CacheLoadRequest> {
		private final Organism organismA;
		private final Organism organismB;
		private final String url;
		private final SpeciesPairProteinOrthologCache cache;
		private final InParanoidCacheLoaderTask task;
		private boolean skipped = false;
		private boolean canceled = false;

		CacheLoadRequest(Organism organismA, Organism organismB, String url, SpeciesPairProteinOrthologCache cache, InParanoidCacheLoaderTask task) {
			this.organismA = organismA;
			this.organismB = organismB;
			this.url = url;
			this.cache = cache;
			this.task = task;
		}

		@Override
		public CacheLoadRequest call() throws Exception {
			if (!task.cancelled) {
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
						} else
							skipped = true;
					}
				} finally {
					if (res != null) res.close();
					if (req != null) req.releaseConnection();
				}
			} else
				canceled = true;
			return this;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		httpClient.close();
	}

}
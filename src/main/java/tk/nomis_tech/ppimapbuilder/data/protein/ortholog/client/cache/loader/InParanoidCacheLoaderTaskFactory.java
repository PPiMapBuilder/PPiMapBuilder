package tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cytoscape.work.*;
import tk.nomis_tech.ppimapbuilder.data.Pair;
import tk.nomis_tech.ppimapbuilder.data.organism.Organism;
import tk.nomis_tech.ppimapbuilder.data.organism.OrganismUtils;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.PMBProteinOrthologCacheClient;
import tk.nomis_tech.ppimapbuilder.data.protein.ortholog.client.cache.SpeciesPairProteinOrthologCache;
import tk.nomis_tech.ppimapbuilder.util.ClassLoaderHack;
import tk.nomis_tech.ppimapbuilder.util.SteppedTaskMonitor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {

	private final static String BASE_URL = "http://inparanoid.sbc.su.se/download/8.0_current/OrthoXML/";

	private final CloseableHttpClient httpClient;
	private final List<Organism> organisms;

	private final ThreadFactory threadFactory;
	private final List<Thread> threads;
	private final Listener listener;

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms) {
		this(organisms, null);
	}

	public InParanoidCacheLoaderTaskFactory(List<Organism> organisms, Listener listener) {
		this.organisms = organisms;
		this.httpClient = HttpClientBuilder.create().build();
		this.listener = listener;

		this.threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				threads.add(t);
				return t;
			}
		};
		this.threads = new ArrayList<Thread>();
	}

	@Override
	public TaskIterator createTaskIterator() {
		Task task = new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				Set<Pair<Organism>> organismCombination = OrganismUtils.createCombinations(organisms);

				//Set number of steps
				SteppedTaskMonitor monitor = new SteppedTaskMonitor(taskMonitor, organismCombination.size() + 1);
				monitor.setTitle("Loading InParanoid OrthoXML files in cache");

				List<Future<CacheLoadRequest>> requests = new ArrayList<Future<CacheLoadRequest>>();
				CompletionService<CacheLoadRequest> completionService = new ExecutorCompletionService<CacheLoadRequest>(Executors.newFixedThreadPool(3, threadFactory));

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
							.getSpeciesPairProteinOrthologCache(organismA, organismB)
					);
					requests.add(completionService.submit(request));

					if (cancelled)
						break;
				}

				Future<CacheLoadRequest> future;
				for (int i = 0, requestsSize = requests.size(); i < requestsSize; i++) {
					try {
						future = completionService.take();

						CacheLoadRequest cacheLoadRequest = future.get();

						monitor.setStep(
							(cacheLoadRequest.skipped ? "Skipped couple: " : "Loaded couple: ")
							+ cacheLoadRequest.organismA.getSimpleScientificName()
							+ " and "
							+ cacheLoadRequest.organismB.getSimpleScientificName()
						);
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}

				monitor.setProgress(1);
				if(listener != null)
					listener.done();
			}

			class CacheLoadRequest implements Callable<CacheLoadRequest> {
				Organism organismA;
				Organism organismB;
				String url;
				SpeciesPairProteinOrthologCache cache;
				boolean skipped = false;

				CacheLoadRequest(Organism organismA, Organism organismB, String url, SpeciesPairProteinOrthologCache cache) {
					this.organismA = organismA;
					this.organismB = organismB;
					this.url = url;
					this.cache = cache;
				}

				@Override
				public CacheLoadRequest call() throws Exception {
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
					return this;
				}
			}

			@Override
			public void cancel() {
				for (Thread thread : threads)
					if (thread.isAlive() && !thread.isInterrupted() && !thread.getState().equals(Thread.State.TERMINATED))
						thread.interrupt();
				Thread.currentThread().interrupt();
			}
		};
		return new TaskIterator(task);
	}

	@Override
	protected void finalize() throws Throwable {
		httpClient.close();
	}

	public interface Listener {
		public void done();
	}
}

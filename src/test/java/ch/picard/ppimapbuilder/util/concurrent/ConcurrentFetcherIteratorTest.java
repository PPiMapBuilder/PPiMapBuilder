package ch.picard.ppimapbuilder.util.concurrent;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConcurrentFetcherIteratorTest {

	@Test public void test() throws InterruptedException {
		Set<String> expected = new HashSet<String>();
		Set<String> actual = new HashSet<String>();

		ArrayList<IteratorRequest<String>> requests = Lists.newArrayList();

		for(int i = 1; i<= 200; i++) {
			final ArrayList<String> string = Lists.newArrayList();
			for(int j = 1; j <= 300; j++) {
				final String e = i + "-" + j;
				string.add(e);
				expected.add(e);
			}
			requests.add(new IteratorRequest<String>() {
				@Override
				public Iterator<String> call() throws Exception {
					Thread.sleep(1000);
					return string.iterator();
				}
			});
		}

		final ExecutorServiceManager serviceManager = new ExecutorServiceManager(5);

		final ConcurrentFetcherIterator<String> fetch = new ConcurrentFetcherIterator<String>(requests, serviceManager);
		while (fetch.hasNext())
			actual.add(fetch.next());
		Assert.assertEquals(expected, actual);
	}
}

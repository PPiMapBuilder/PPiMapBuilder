/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

	/**
	 * Fetch an URL and parse it to a Jsoup document with multiple attempt by configuring retries, timeout and sleep temporizing.
	 *
	 * @param URL              source http URL
	 * @param baseTimeout      base timeout at first try
	 * @param timeoutIncrement value that will be added to the base timeout at each new try
	 * @param maxRetry         max number of tries
	 * @param sleepTemporizing sleep time between each try in milliseconds
	 */
	public static Document getDocumentWithRetry(String URL, int baseTimeout, int timeoutIncrement, int maxRetry, int sleepTemporizing) throws IOException {
		final Document[] document = new Document[]{null};
		fetchURLWithRetryAsInputStream(
				URL,
				new InputStreamConsumer() {
					@Override
					public void accept(InputStream inputStream) throws IOException {
						document[0] = Jsoup.parse(
								inputStream,
								"UTF-8",
								"/"
						);
					}
				},
				baseTimeout,
				timeoutIncrement,
				maxRetry,
				sleepTemporizing
		);
		return document[0];
	}


	/**
	 * Fetch an URL as InputStream with multiple attempt by configuring retries, timeout and sleep temporizing.
	 *
	 * @param URL              source http URL
	 * @param consumer         an InputStreamConsumer that will use the InputStream fetched from the URL
	 * @param baseTimeout      base timeout at first try
	 * @param timeoutIncrement value that will be added to the base timeout at each new try
	 * @param maxRetry         max number of tries
	 * @param sleepTemporizing sleep time between each try in milliseconds
	 */
	public static boolean fetchURLWithRetryAsInputStream(
			String URL,
			InputStreamConsumer consumer,
			int baseTimeout,
			int timeoutIncrement,
			int maxRetry,
			int sleepTemporizing
	) throws IOException {
		int tryCount = 0;
		DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(1, true);
		CloseableHttpClient httpClient = null;

		IOException error = null;
		while (tryCount <= maxRetry) {
			httpClient = HttpClients.custom()
					.setRetryHandler(retryHandler)
					.setDefaultRequestConfig(RequestConfig.custom()
							//.setSocketTimeout(baseTimeout + (tryCount * timeoutIncrement))
							.setConnectTimeout(baseTimeout + (tryCount * timeoutIncrement))
							//.setConnectionRequestTimeout(baseTimeout + (tryCount * timeoutIncrement))
							.build())
					.build();

			if (tryCount > 0 && sleepTemporizing > 0)
				try {
					Thread.sleep(sleepTemporizing);
				} catch (InterruptedException ignored) {
				}
			tryCount++;

			HttpRequestBase req = null;
			CloseableHttpResponse res = null;
			try {
				req = new HttpGet(URL);
				res = httpClient.execute(req);

				int statusCode = res.getStatusLine().getStatusCode();

				if (200 <= statusCode && statusCode < 300) {
					//System.out.println("success-"+tryCount);
					consumer.accept(res.getEntity().getContent());
					return true;
				} else {
					//System.out.println(statusCode+"-"+tryCount);
					return false;
				}
			} catch (IOException e) {
				error = new IOException(e.getMessage() + " " + URL);
			} finally {
				if (res != null) res.close();
				if (req != null) req.releaseConnection();
				httpClient.close();
			}
		}
		//System.out.println("fail-"+tryCount);
		if (error != null) throw error;
		return false;
	}

	public static interface InputStreamConsumer {
		public void accept(InputStream inputStream) throws IOException;
	}

}

package ch.picard.ppimapbuilder.data.interaction.client.web;

import junit.framework.Assert;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PsicquicRequestBuilderTest {

	private static PsicquicSimpleClient createMockPsicquicSimpleClient(long countByQuery) throws IOException {
		final PsicquicSimpleClient client = mock(PsicquicSimpleClient.class);
		when(client.countByQuery(anyString())).thenReturn(countByQuery);
		return client;
	}

	@Test
	public void testMultipleClients() throws IOException {
		final String query = "DUMMY QUERY";
		final int nbClients = 5;

		// Generate 5 clients
		List<PsicquicSimpleClient> clients = new ArrayList<PsicquicSimpleClient>();
		for (int i = 1; i <= nbClients; i++) {
			clients.add(createMockPsicquicSimpleClient(1l));
		}

		// Ask for 1 query
		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(clients)
				.addQuery(query);

		final List<PsicquicRequest> requests = builder.getPsicquicRequests();
		Assert.assertNotNull(requests);
		Assert.assertFalse(requests.isEmpty());
		Assert.assertEquals(requests.size(), nbClients);
		for (PsicquicRequest request : requests) {
			Assert.assertEquals(request.getQuery(), query);
		}
	}

	@Test
	public void testMultiplePages() throws IOException {
		final String query = "DUMMY QUERY";
		final int maxResultsPerPages = 3;

		List<PsicquicSimpleClient> clients = Arrays.asList(createMockPsicquicSimpleClient(16l));

		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(clients)
				.setMaxResultsPerPages(maxResultsPerPages)
				.addQuery(query);

		final List<PsicquicRequest> requests = builder.getPsicquicRequests();
		Assert.assertNotNull(requests);
		Assert.assertFalse(requests.isEmpty());
		Assert.assertEquals(requests.size(), 6);
		int i = 0;
		for (PsicquicRequest request : requests) {
			Assert.assertEquals(i * maxResultsPerPages, request.getFirstResult());
			Assert.assertEquals(maxResultsPerPages, request.getMaxResults());
			Assert.assertEquals(query, request.getQuery());
			i++;
		}
	}

	@Test
	public void testAddGetByTaxon() throws IOException {
		List<PsicquicSimpleClient> clients = Arrays.asList(createMockPsicquicSimpleClient(1l));

		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(clients)
				.addGetByTaxon(9606);

		final List<PsicquicRequest> requests = builder.getPsicquicRequests();
		Assert.assertNotNull(requests);
		Assert.assertFalse(requests.isEmpty());
		Assert.assertEquals(requests.size(), 1);
		final String actualQuery = requests.get(0).getQuery().trim();
		final String expectedQuery = "taxidA:9606 AND taxidB:9606";
		Assert.assertEquals(expectedQuery, actualQuery);
	}

	@Test
	public void testAddGetByTaxonAndId() throws IOException {
		List<PsicquicSimpleClient> clients = Arrays.asList(createMockPsicquicSimpleClient(1l));

		final PsicquicRequestBuilder builder = new PsicquicRequestBuilder(clients)
				.addGetByTaxonAndId("P04040", 9606);

		final List<PsicquicRequest> requests = builder.getPsicquicRequests();
		Assert.assertNotNull(requests);
		Assert.assertFalse(requests.isEmpty());
		Assert.assertEquals(requests.size(), 1);
		final String actualQuery = requests.get(0).getQuery().trim();
		final String expectedQuery = "taxidA:9606 AND taxidB:9606 AND id:P04040";
		Assert.assertEquals(expectedQuery, actualQuery);
	}

}

package tk.nomis_tech.ppimapbuilder.util;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class ThreadedPsicquicSimpleClientTest {

	static ThreadedPsicquicSimpleClient client;

	@BeforeClass
	public static void init() throws IOException {
		List<PsicquicService> services = PsicquicRegistry.getInstance().getServices();
		client = new ThreadedPsicquicSimpleClient(services, services.size());
	}

	@Test
	public void testGetByQuery() {
		System.out.println(client.getByQuery("identifier:P04040").size());
	}

}

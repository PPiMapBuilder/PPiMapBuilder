package tk.nomis_tech.ppimapbuilder.webservice;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import tk.nomis_tech.ppimapbuilder.webservice.PsicquicRegistry;
import tk.nomis_tech.ppimapbuilder.webservice.PsicquicService;

public class PsicquicRegistryTest {

	@Test
	public void test() throws IOException {
		List<PsicquicService> services = PsicquicRegistry.getInstance().getServices();
		assertTrue(!services.isEmpty());
	}

}

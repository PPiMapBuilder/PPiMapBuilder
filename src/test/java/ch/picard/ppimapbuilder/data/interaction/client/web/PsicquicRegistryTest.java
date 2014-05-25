package ch.picard.ppimapbuilder.data.interaction.client.web;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class PsicquicRegistryTest {

	@Test
	public void test() throws IOException {
		List<PsicquicService> services = PsicquicRegistry.getInstance().getServices();
		assertTrue(!services.isEmpty());
	}

}

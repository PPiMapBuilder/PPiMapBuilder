package tk.nomis_tech.ppimapbuilder.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import psidev.psi.mi.tab.model.BinaryInteraction;

import com.google.common.collect.Lists;

public class ThreadedPsicquicSimpleClientTest {

	static ThreadedPsicquicSimpleClient client;

	@BeforeClass
	public static void init() throws IOException {
		List<PsicquicService> services;
		/*services = Lists.newArrayList(new PsicquicService[] {
				new PsicquicService("BioGrid", null, "http://tyersrest.tyerslab.com:8805/psicquic/webservices/current/search/", "true",
						"1", null, null, "", null),
				new PsicquicService("IntAct", null, "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/",
						"true", "1", null, null, "", null) 
		});
		services = PsicquicRegistry.getInstance().getServices();*/

		services = new ArrayList<PsicquicService>() {
			{
				add(PsicquicRegistry.getInstance().getService("intact", false));
				add(PsicquicRegistry.getInstance().getService("bind", false));
				add(PsicquicRegistry.getInstance().getService("biogrid", false));
				add(PsicquicRegistry.getInstance().getService("dip", false));
				add(PsicquicRegistry.getInstance().getService("uniprot", false));
				add(PsicquicRegistry.getInstance().getService("apid", false));
				add(PsicquicRegistry.getInstance().getService("mint", false));
			}
		};

		client = new ThreadedPsicquicSimpleClient(services, 3);
	}

	@Test
	public void testGetByQuery() throws Exception {
		List<BinaryInteraction> byQuery = client.getByQuery("identifier:P04040");
		
		//System.out.println(byQuery.size());
	}

	@Test
	public void testGetByQueries() throws Exception {
		List<String> queries = Lists.newArrayList(new String[] { 
			"identifier:P04040", 
			"identifier:P61106", 
			"identifier:Q70EK8" 
		});
		List<BinaryInteraction> byQueries = client.getByQueries(queries);
		
		//System.out.println(byQueries.size());
	}

}

package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import com.google.common.collect.Lists;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.junit.BeforeClass;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InteractionUtilsTest {

	private static List<BinaryInteraction> sampleBinaryInteractions;
	private static List<PsicquicService> services;

	@BeforeClass
	public static void init() throws IOException, PsimiTabException {
		services = new ArrayList<PsicquicService>() {{
			add(PsicquicRegistry.getInstance().getService("intact", false));
			add(PsicquicRegistry.getInstance().getService("bind", false));
//			add(PsicquicRegistry.getInstance().getService("biogrid", false));
//			add(PsicquicRegistry.getInstance().getService("uniprot", false));
//			add(PsicquicRegistry.getInstance().getService("dip", false));
//			add(PsicquicRegistry.getInstance().getService("apid", false));
//			add(PsicquicRegistry.getInstance().getService("mint", false));
		}};

		PsicquicSimpleClient client = new PsicquicSimpleClient(PsicquicRegistry.getInstance().getService("intact", false).getRestUrl());
		PsimiTabReader mitabReader = new PsimiTabReader();
		InputStream result = client.getByQuery("P04040", PsicquicSimpleClient.MITAB25);

		sampleBinaryInteractions = Lists.newArrayList(mitabReader.read(result));
	}

	@Test
	public void getInteractors() {
		Set<String> interactors = InteractionUtils.getInteractors(sampleBinaryInteractions);
		// System.out.println(interactors);
	}


	// @Test
	public void networkExpansion() throws Exception {
		List<BinaryInteraction> res;
		Set<String> prots;
		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(services, 5);
		MiQLParameterBuilder query = new MiQLParameterBuilder("identifier", "P04040");

		int i = 0;
		do {
			prots = InteractionUtils.getInteractors(client.getByQuery(query.toString()));
			System.out.println(prots);
			System.out.println(prots.size());
			MiQLExpressionBuilder protsE = new MiQLExpressionBuilder();
			protsE.addAll(Lists.newArrayList(prots));
			query = new MiQLParameterBuilder("id", protsE);
			i++;
		} while (i < 2);
	}


	@Test
	public void testFilterNonUniProt() throws Exception {
		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(PsicquicRegistry.getInstance().getServices(), 3);
		List<BinaryInteraction> byQuery = client.getByQuery("id:P04040");

		System.out.println(byQuery.size());
		byQuery = InteractionUtils.filter(byQuery, new InteractionUtils.UniProtInteractionFilter());
		System.out.println(byQuery.size());
	}

}

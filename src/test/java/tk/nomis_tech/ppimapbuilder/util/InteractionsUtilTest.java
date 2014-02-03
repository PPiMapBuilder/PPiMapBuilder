package tk.nomis_tech.ppimapbuilder.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.junit.BeforeClass;
import org.junit.Test;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLExpressionBuilder;
import tk.nomis_tech.ppimapbuilder.util.miql.MiQLParameterBuilder;

import com.google.common.collect.Lists;

public class InteractionsUtilTest {

	private static List<BinaryInteraction> sampleBinaryInteractions;
	private static List<PsicquicService> services;

	@BeforeClass
	public static void init() throws IOException, PsimiTabException {
		services = (new PsicquicRegistry()).getServices();

		PsicquicSimpleClient client = new PsicquicSimpleClient(
				"http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/");
		PsimiTabReader mitabReader = new PsimiTabReader();
		InputStream result = client.getByQuery("P04040", PsicquicSimpleClient.MITAB25);

		sampleBinaryInteractions = Lists.newArrayList(mitabReader.read(result));
	}

	@Test
	public void getInteractorsBinary() {
		List<String> interactors = InteractionsUtil.getInteractorsBinary(sampleBinaryInteractions);
		// System.out.println(interactors);
	}

	@Test
	public void getInteractionBetweenProtein() throws Exception {
		HashSet<String> prots = new HashSet<String>(Arrays.asList(new String[] { "Q53YK7", "P27635", "Q5NGF9", "Q9UGK8", "O15350-1",
				"Q5NGF3", "P53621", "P07101", "P41134", "Q8NBU8", "P98077", "P14907", "Q9UGJ8", "Q5NGE9", "Q9NUY6", "Q96SB8", "Q9UGJ0",
				"P13073", "P47901", "P47900", "Q9P1A6", "P98082"}));
		List<BinaryInteraction> res = InteractionsUtil.getInteractionBetweenProtein(prots, 9606, services);
		System.out.println(res.size());

	}

	// @Test
	public void networkExpansion() {
		List<BinaryInteraction> res;
		List<String> prots;
		ThreadedPsicquicSimpleClient client = new ThreadedPsicquicSimpleClient(services, 5);
		MiQLParameterBuilder query = new MiQLParameterBuilder("identifier", "P04040");

		prots = InteractionsUtil.getInteractorsBinary(client.getByQuery(query.toString()));
		System.out.println(prots);
		System.out.println(prots.size());
		MiQLExpressionBuilder protsE = new MiQLExpressionBuilder();
		protsE.addAll(Lists.newArrayList(prots));
		query = new MiQLParameterBuilder("id", protsE);

		System.out.println(query.toString());
		for (int i = 0; i < 2; i++) {
			prots = InteractionsUtil.getInteractorsBinary(client.getByQuery(query.toString()));
			System.out.println(prots);
			System.out.println(prots.size());
			protsE = new MiQLExpressionBuilder();
			protsE.addAll(Lists.newArrayList(prots));
			query = new MiQLParameterBuilder("id", protsE);

			System.out.println(query.toString());
		}
	}

}

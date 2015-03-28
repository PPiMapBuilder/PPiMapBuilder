package ch.picard.ppimapbuilder.data.interaction.client.web;

import com.google.common.collect.Lists;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.junit.BeforeClass;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteractionUtilsTest {

	private static List<BinaryInteraction> sampleBinaryInteractions;
	private static final List<PsicquicService> services = Arrays.asList(
			new PsicquicService("IntAct", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("BioGrid", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("BIND", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("DIP", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("MINT", null, "", "true", "1", "", "", "true", new ArrayList<String>())
	);

	@BeforeClass
	public static void init() throws IOException, PsimiTabException {
		PsicquicSimpleClient client = new PsicquicSimpleClient(PsicquicRegistry.getInstance().getService("intact", false).getRestUrl());
		PsimiTabReader mitabReader = new PsimiTabReader();
		InputStream result = client.getByQuery("P04040", PsicquicSimpleClient.MITAB25);

		sampleBinaryInteractions = Lists.newArrayList(mitabReader.read(result));
	}

}

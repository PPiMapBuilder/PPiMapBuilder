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
    
package ch.picard.ppimapbuilder.data.interaction.client.web;

import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLExpressionBuilder;
import ch.picard.ppimapbuilder.data.interaction.client.web.miql.MiQLParameterBuilder;
import ch.picard.ppimapbuilder.data.protein.Protein;
import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import ch.picard.ppimapbuilder.util.concurrency.ExecutorServiceManager;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

	@Test
	public void getInteractors() {
		Set<Protein> interactors = InteractionUtils.getInteractors(sampleBinaryInteractions);
		// System.out.println(interactors);
	}


	// @Test
	public void networkExpansion() throws Exception {
		List<BinaryInteraction> res;
		Set<Protein> prots;
		ThreadedPsicquicClient client = new ThreadedPsicquicClient(services, new ExecutorServiceManager(5));
		MiQLParameterBuilder query = new MiQLParameterBuilder("identifier", "P04040");

		int i = 0;
		do {
			prots = InteractionUtils.getInteractors(client.getByQuery(query.toString()));
			System.out.println(prots);
			System.out.println(prots.size());
			MiQLExpressionBuilder protsE = new MiQLExpressionBuilder();
			protsE.addAll(new ArrayList<String>(ProteinUtils.asIdentifiers(prots)));
			query = new MiQLParameterBuilder("id", protsE);
			i++;
		} while (i < 2);
	}


	@Test
	public void testFilterNonUniProt() throws Exception {
		ThreadedPsicquicClient client = new ThreadedPsicquicClient(services, new ExecutorServiceManager(3));
		List<BinaryInteraction> byQuery = client.getByQuery("id:P04040");

		System.out.println(byQuery.size());
		byQuery = InteractionUtils.filter(byQuery, new InteractionUtils.UniProtInteractionFilter());
		System.out.println(byQuery.size());
	}

}

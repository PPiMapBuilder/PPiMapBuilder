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

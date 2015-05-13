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
    
package ch.picard.ppimapbuilder.data.protein.client.web;

import ch.picard.ppimapbuilder.data.protein.UniProtEntry;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UniProtEntryClientTest {

	private static UniProtEntryClient uniProtEntryClient;
	private static List<String> prots;

	@BeforeClass
	public static void init() {
		uniProtEntryClient = new UniProtEntryClient(new ExecutorServiceManager(6));
		prots = Arrays.asList("P04040", "P61106", "Q70EK8", "Q96RU2", "Q92621", "Q9BRV8", "Q92624", "Q9NV70", "P13804",
				"Q9UMJ3", "Q8ZAB3", "P13807", "Q5Y7A7", "O00629", "O00628", "Q96S59", "O00623", "P22455", "P04637", "Q8TDI0", "Q92633",
				"P04632", "Q8WVM8", "Q5NH30", "Q8ZAC2");
		System.out.println("Testing UniProtEntryClient on " + prots.size() + " proteins");
	}

	@Test
	public void test() throws IOException {
		HashMap<String, UniProtEntry> results = new HashMap<String, UniProtEntry>();

		for (String prot : prots) {
			UniProtEntry retrieveProteinData = uniProtEntryClient.retrieveProteinData(prot);
			if (retrieveProteinData != null) {
				results.put(prot, retrieveProteinData);
			}
		}
		System.out.println(results);
	}

	@Test
	public void test1() throws IOException {
		HashMap<String, UniProtEntry> retrieveProteinsData = uniProtEntryClient.retrieveProteinsData(prots);
		System.out.println(retrieveProteinsData);
	}

	@Test
	public void testError() throws IOException {
		UniProtEntry expected = null;
		UniProtEntry actual = uniProtEntryClient.retrieveProteinData("TSDF");
		Assert.assertEquals(expected, actual);
	}
}

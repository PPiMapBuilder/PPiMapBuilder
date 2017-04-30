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

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.PairUtils;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.util.ProgressMonitor;
import ch.picard.ppimapbuilder.util.concurrent.ExecutorServiceManager;
import org.junit.Assert;
import org.junit.Test;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * /!\ ThreadedPsicquicClient will be replaced by PsicquicRequestBuilder
 */
@Deprecated
public class ThreadedPsicquicClientTest {

	private static final List<PsicquicService> services = Arrays.asList(
			new PsicquicService("IntAct", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("BioGrid", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("BIND", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("DIP", null, "", "true", "1", "", "", "true", new ArrayList<String>())
			, new PsicquicService("MINT", null, "", "true", "1", "", "", "true", new ArrayList<String>())
	);

	private static final ProgressMonitor assertingCorrectProgression = new ProgressMonitor() {
		private final double[] progress = new double[]{0d};
		@Override
		public void setProgress(double v) {
			synchronized (progress) {
				Assert.assertTrue(v > progress[0]);
				progress[0] = v;
				System.out.println("[PROGRESS] "+(v*100)+"%");
			}
		}
	};

	@Test
	public void testGetByQuery() throws Exception {
		for(int nbThread = 1; nbThread <= 10; nbThread++) {
			System.out.println(nbThread+" THREADS");
			final String query = "identifier:P04040";

			final Map<String, String> expected = new HashMap<String, String>() {{
				for (PsicquicService service : services) {
					put(service.getName(), query);
				}
			}};

			final Map<String, String> actual = new HashMap<String, String>();
			ThreadedPsicquicClient client = new ThreadedPsicquicClient(services, new ExecutorServiceManager(nbThread)) {
				@Override
				protected List<BinaryInteraction> getByQuerySimple(PsicquicService service, String query, ProgressMonitor monitor) throws IOException, PsimiTabException {
					monitor.setProgress(0.1);
					monitor.setProgress(0.2);
					monitor.setProgress(0.5);
					actual.put(service.getName(), query);
					monitor.setProgress(0.8);
					monitor.setProgress(1);
					return null;
				}
			};

			client.getByQuery(
					query,
					assertingCorrectProgression
			);

			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testGetByQueries() throws Exception {
		final Set<String> queries = new TreeSet<String>(Arrays.asList(
				"identifier:P04040",
				"identifier:P61106",
				"identifier:Q70EK8"
		));

		final Map<String, Set<String>> expected = new TreeMap<String, Set<String>>() {{
			for (PsicquicService service : services) {
				put(service.getName(), queries);
			}
		}};

		Error error = null;
		for(int nbThread = 1; nbThread <= 10; nbThread++) {
			final Map<String, Set<String>> actual = new TreeMap<String, Set<String>>();
			ThreadedPsicquicClient client = new ThreadedPsicquicClient(services, new ExecutorServiceManager(nbThread)) {
				@Override
				protected List<BinaryInteraction> getByQuerySimple(PsicquicService service, String query, ProgressMonitor monitor) throws IOException, PsimiTabException {
					if (!actual.containsKey(service.getName()))
						actual.put(service.getName(), new TreeSet<String>());

					if (!actual.get(service.getName()).add(query))
						Assert.fail("Duplicated query");

					return null;
				}
			};
			client.getByQueries(queries);

			try {
				Assert.assertEquals(expected, actual);
			} catch (AssertionError e) {
				error = e;
				e.printStackTrace();
			}
		}

		if(error != null)
			throw error;
	}


	@Test
	public void testGetInteractionsInProteinPool() throws Exception {
		final Set<Set<String>> expected = new HashSet<Set<String>>() {{
			for (Pair<String> pair : PairUtils.createCombinations(proteins, true, false)) {
				add(new HashSet<String>(Arrays.asList(pair.getFirst(), pair.getSecond())));
			}
		}};

		for(int nbThread = 1; nbThread <= 10; nbThread++) {
			final Set<Pair<String>> result = new TreeSet<Pair<String>>();

			final Pattern idAPattern = Pattern.compile(".*idA:\\((.*)\\)\\s.*");
			final Pattern idBPattern = Pattern.compile(".*idB:\\((.*)\\).*");

			final Set<String> treatedQuery = new HashSet<String>();

			ThreadedPsicquicClient client = new ThreadedPsicquicClient(services, new ExecutorServiceManager(nbThread)) {
				@Override
				protected List<BinaryInteraction> getByQuerySimple(PsicquicService service, String query, ProgressMonitor monitor) throws IOException, PsimiTabException {
					synchronized (treatedQuery) {
						if(treatedQuery.contains(query))
							return null;

						treatedQuery.add(query);
					}

					Matcher m = idAPattern.matcher(query);
					m.find();
					String[] idsA = m.group(1).split("\\s");
					m = idBPattern.matcher(query);
					m.find();
					String[] idsB = m.group(1).split("\\s");

					synchronized (result) {
						for(String idA : idsA) {
							for(String idB: idsB) {
								if(idA.compareTo(idB) < 0)
									result.add(new Pair<String>(idA, idB));
								else
									result.add(new Pair<String>(idB, idA));

							}
						}
					}

					return null;
				}
			};
			client.getInteractionsInProteinPool(proteins, human);

			final Set<Set<String>> actual = new HashSet<Set<String>>() {{
				for (Pair<String> pair : result) {
					add(new HashSet<String>(Arrays.asList(pair.getFirst(), pair.getSecond())));
				}
			}};

			Assert.assertEquals(expected, actual);
		}
	}

	private static final Organism human = new Organism("Homo sapiens", 9606);
	private static final Set<String> proteins = new HashSet<String>(Arrays.asList(new String[]{"Q53YK7", "P27635", "Q5NGF9", "Q9UGK8", "O15350",
					"Q5NGF3", "P53621", "P07101", "P41134", "Q8NBU8", "P98077", "P14907", "Q9UGJ8", "Q5NGE9", "Q9NUY6", "Q96SB8", "Q9UGJ0",
					"P13073", "P47901", "P47900", "Q9P1A6", "P98082", "P21809", "Q96SA5", "P13861", "A0AV37", "Q53GK5", "P41743", "Q9UBU3",
					"P40616", "Q8TCX1", "Q9H4L7", "Q494Z3", "P14921", "P14920", "P35590", "P14923", "A1Z5I9", "P06576", "P13051", "Q16716",
					"Q6AI08", "P53611", "P21810", "Q9Z1S8", "Q8N8S7", "P53618", "Q8WVD3", "Q76L83", "P24409", "Q9NNW8", "O14490", "A2RU21",
					"P41180", "P41182", "Q6JVM3", "P18085", "P31016", "O15553", "Q9UGP2", "Q5NGJ7", "Q5NGJ5", "Q9UGP5", "P18089", "P07148",
					"Q5NGJ9", "P03107", "P01556", "Q9NNX0", "P53675", "Q569J5", "P01566", "P01567", "P01568", "P01569", "P01562", "P01563",
					"P03905", "P42285", "Q59F66", "P41159", "P01570", "Q5TCL9", "P27661", "P34820", "P01571", "P03101", "P01579", "P01574",
					"P03915", "Q74RB7", "P26640", "Q5NGK2", "P41143", "Q9UMA4", "P41146", "Q8NC36", "Q5NGK0", "P41145", "Q99816", "P48556",
					"F5GWT4", "Q9HBG7", "Q99814", "Q9NUR3", "Q15102", "P30793", "Q8NBL8", "O43318", "Q9P0V3", "Q5NH00", "Q9HBG4", "O43313"/*,
					"Q96NI0", "P47989", "E7EU02", "Q9HBH9", "Q8WW24", "P36956", "Q5NGM7", "P36956", "Q15109", "P02649", "Q15911", "O43324",
					"P17706", "O43320", "Q6N0A4", "P02647", "Q9H4B7", "Q9BRQ5", "Q6NXR4", "Q9NV58", "Q99836", "Q03113", "Q96EG7", "Q5NH17",
					"Q5NH18", "P40692", "Q5NH19", "Q643R0", "Q15121", "Q99832", "Q5NGP6", "P62195", "Q15116", "P31040", "P52907", "Q15119",
					"P97710", "Q7CK91", "Q15904", "Q92600", "Q15907", "P28749", "B8PSA7", "Q15906", "Q16795", "Q86SG6", "P27694", "P27695",
					"P30414", "Q92616", "Q5NH06", "P07196", "P07195", "P30411", "P18031", "O43306", "Q15126", "P04085", "P12036", "Q9H4D4",
					"P04085", "P03126", "Q92610", "P33316", "P42224", "Q96EA4", "Q96RU7", "P14174", "P22466", "Q62313", "Q03135", "P42229",
					"Q96RU2", "Q92621", "Q9BRV8", "Q92624", "Q9NV70", "P13804", "Q9UMJ3", "Q8ZAB3", "P13807", "Q5Y7A7", "O00629", "O00628",
					"Q96S59", "O00623", "P22455", "P04637", "Q8TDI0", "Q92633", "P04632", "Q8WVM8", "Q5NH30", "Q8ZAC2", "P62191", "P12018",
					"Q71SY5", "Q96RS2", "Q6P3U7", "Q96RS0", "Q96S46", "P59768", "P29275", "P29274", "P50613", "P62158", "P16298", "Q9HBI1",
					"Q5BKU3", "Q6IN79", "Q9NV92", "P04626", "Q5NH64", "Q7CK56", "Q9BS50", "P04629", "B7Z1G5", "P12004", "Q9UMH7", "Q96EB6",
					"P34896", "Q9NUW8", "P32519", "P16284", "P34897", "P13010", "P14142", "P14136", "P04614", "Q9UMI3", "Q5NH53", "Q5NH54",
					"Q13888", "Q9P0U3", "Q5NH75", "Q13868", "Q13867", "Q9HC13", "P50607", "A8K654", "Q9BSJ8", "O43290", "Q15070", "O43291",
					"P62269", "P62266", "Q15075", "Q15077", "P62263", "P23396", "B3KSG3", "Q5NGW8", "Q5NH87", "P80075", "Q8WV81", "Q8WUW1",
					"P10515", "Q01844", "Q59EB8", "Q9Z2F7", "Q92664", "P62258", "O43280", "Q9BSK3", "O43281", "Q8Z9W7", "Q96T48", "O00712",
					"Q99778", "Q15067", "O00716", "P09496", "Q5NGX8", "Q5NGX7", "Q13840", "Q96F07", "P49286", "O95149", "P62249", "P00492",
					"F5H0L1", "P00491", "O60739", "P43403", "Q0VGA5", "P43405", "P09486", "P62241", "P09488", "Q14451", "P62244", "Q9UGY1",
					"Q5NGY9", "P23378", "Q5NGY8", "Q96EP5", "Q9P0P0", "Q5NGY1", "O95136", "O60721", "Q13838", "Q96EP1", "P00480", "O43261",
					"Q15084", "F1T0G6", "Q96T23", "Q99798", "Q96ML7", "Q9UM22", "O43242", "Q59EE8", "P02686", "Q15027", "Q8IU60", "Q9HC57",
					"Q99741", "Q99740", "Q9NW08", "Q96SN7", "Q15036", "Q8NCA5", "P05023", "Q9UM13", "Q9UM11", "P20290", "P49257", "Q15019",
					"O43237", "Q9UHA4", "Q13813", "Q8Z9S4", "Q15020", "B2REC0", "Q8IU54", "Q99731", "Q9NVP1", "P49247", "Q01892", "Q4VC05",
					"P05014", "P05015", "Q9BT06", "Q96EI6", "Q8Z9S5", "P05013", "B2RNB2", "P56192", "Q9UM07", "P29317", "Q9P0M0", "P00441",
					"Q7CL03", "Q99767", "Q7CL02", "Q5JRX3", "F8VQP3", "P29323", "Q9HC29", "Q8TDN4", "Q9HC23", "Q9GIY3", "P52948", "Q15047",
					"P37198", "P16333", "Q99759", "Q96EK6", "Q59EE4", "Q99757", "Q9UM72", "P35520", "Q9ULV5", "P53597", "Q9HBY0", "O15520",
					"P08865", "O15527", "O15524", "O15530", "O35280", "A6H8L7", "Q8TDU9", "Q53YD7", "Q5W007", "P15260", "O15519", "Q6PCD5",
					"P16310", "Q9ULU4", "O15511", "P20248", "Q53GA5", "Q9NVC6", "Q2PYN1", "O95197", "Q08945", "P10599", "A8K5M4", "Q9UM55",
					"Q02447", "Q02446", "Q9P0I2", "Q96MU7", "Q96SH1", "P43487", "Q9HBW0", "P08887", "P84243", "O15552", "P10589", "P10586",
					"A1L0U9", "Q8WVC6", "O75122", "Q9NVD7", "Q5UIP0", "Q8TDS4", "B7Z9Q2", "Q8TDS5", "Q9UM47", "Q96F88", "P28221", "Q5SW96",
					"Q9UH32", "P28223", "P28222", "Q96MT8", "P20226", "O15534", "O75116", "O94778", "P55075", "P09467", "Q9ULZ1", "Q9BSF8",
					"Q9ULZ3", "P35568", "Q9ULZ9", "P21796", "O60755", "P35570", "Q96MT3", "Q02410", "Q5NHC6", "Q53QE4", "Q99062", "Q96EQ0",
					"O95166", "Q5SW79", "F5GXI9", "Q9NVH6", "Q59F02", "A0AV56", "Q00266", "P41217", "Q9UH99", "P21781", "Q9ULY4", "Q59F09",
					"P35579", "O95157", "Q96ER3", "Q96ER7", "O95154", "O15504", "Q9UGU0", "Q9NVI7", "Q9NVI1", "Q9BSD8", "P06493", "Q9ULX3",
					"Q9UGU5", "Q99081", "O95185", "Q96SD1", "Q8TDW5", "Q5NHF3", "P35557", "P15289", "Q9UGV6", "P07203", "Q9UH77", "Q5NHF4",
					"Q8TE77", "O60763", "A8K5G0", "Q14416", "Q7LG56", "P09429", "Q5TC07", "O75962", "P13984", "Q8AZM1", "P12872", "Q86U22",
					"O15498", "Q16829", "P28062", "Q16828", "Q9P2D7", "P17661", "P28065", "Q16827", "P28066", "O75955", "Q9NP97", "Q9BZZ3",
					"Q9BQ51", "P12883", "Q9NP95", "P28074", "P28072", "P28070", "Q8ZIM7", "Q07325", "P07237", "Q5NHI8", "Q16836", "Q8N9S6",
					"Q08050", "Q7CJA4", "Q7CJA6", "Q8WUF5", "Q16850", "P33240", "B7Z897", "Q9BQ63", "Q9H3G6", "Q9P2A9", "Q05193", "Q9P2B2",
					"P22307", "Q5NHH5", "P22303", "Q8N9T8", "B2R4P8", "P07225", "Q9NTW7", "Q5HYJ3", "Q9UFH4", "B7WPI0", "Q9BPV8", "Q9NP72",
					"Q9BPV1", "Q9H3H8", "O75971", "Q9H3I1", "P17676", "Q9HCB6", "P40763", "Q9JME5", "P60228", "Q9BQ83", "Q9BPW8", "B2RNT7",
					"Q0VG69", "O75152", "Q8WUD1", "P61353", "Q8IUC6", "Q9BQ89", "Q96LW7", "Q9HCC0", "Q9H3N1", "O15457", "O15455", "Q96CX2",
					"Q9H3N8", "Q06830", "P42167", "P11717", "Q9UFJ2", "Q8IUD2", "P21730", "P21731", "O15467", "Q96CW1", "Q9BPX1", "P14209",
					"P27797", "Q9HCE1", "Q9HCE7", "P08670", "Q8ZIL9", "O75182", "Q5NHL3", "P79483", "Q5U623", "O00585", "Q9H3M7", "P47825",
					"Q8ZIK6", "O15444", "O15446", "Q9UNE7", "Q96DE8", "P30542", "P31153", "P42126", "O00571", "Q99714", "Q86TI0", "O00574",
					"Q99717", "A8K003", "F1D8P1", "Q99719", "P18146", "Q6FGR6", "Q9BZQ0", "P02533", "Q7CJ78", "Q9UNF1", "Q5NI40", "Q5NHQ9",
					"P31146", "P30530", "Q9NPB6", "Q5SXM2", "P22352", "P30533", "O00567", "Q92738", "P02545", "Q92731", "Q86TH5", "Q5NHR3",
					"Q15008", "Q15007", "Q5NI31", "Q7CJ89", "P31150", "P02549", "Q5NHP3", "Q5NHP5", "A8IE48", "P30679", "P12814", "P48681",
					"P22392", "P02511", "Q7CJ91", "Q9UNH5", "Q86YH7", "Q53XR6", "P30550", "Q8ZIG8", "Q8ZIG9", "Q99708", "P29144", "P30559",
					"P05067", "O00541", "P30556", "Q99704", "Q96SZ6", "Q99705", "Q86YI0", "Q6FH48", "Q6FH47", "P47871", "P12829", "F1D8T1",
					"Q9UNI7", "Q8ZIH1", "P12821", "Q8ZIH0", "P47898", "Q16864", "Q701P4", "F5GXX7", "Q96T66", "Q92769", "P47897", "P62277",
					"Q5NI78", "Q53Y49", "A6H8U5", "P62280", "A6H8V0", "Q92766", "Q9P276", "P17612", "Q9BR39", "B9A6L2", "Q8WUM0", "Q96T51",
					"Q96ST3", "P05089", "Q96T58", "Q9NPF7", "Q5NI76", "Q9H3F6", "P17600", "Q9BQ15", "Q9BQQ6", "Q8N7D9", "Q5VWC9", "Q92748",
					"Q92747", "P81605", "P18124", "P03087", "O00512", "B3KQF8", "P30520", "Q9NPC1", "P05091", "A6NJ78", "Q9NPB8", "Q9HCM9",
					"Q9P258", "Q92743", "Q5K546", "Q9BZS1", "Q9HCM3", "P43628", "P30518", "P43626", "P03070", "P25490", "O00505", "Q9NPD3",
					"Q5NHS9", "P10721", "A6H8Y1", "Q9UNM6", "Q9H3D4", "Q9HCN4", "P30519", "Q9BQS8", "Q6ZT60", "Q9P246", "Q9P244", "O60613",
					"P82094", "O95260", "Q8ZAA1", "Q9BZI1", "Q14330", "Q00987", "Q99683", "Q9GZX7", "Q9BQE3", "O43390", "A6NIB2", "Q53X72",
					"Q9UFW8", "P29992", "B4DUS6", "O43379", "P10619", "O60602", "Q5VW80", "Q8WTW2", "P23258", "B7Z8H0", "Q8IUX2", "Q7CJP1",
					"Q13952", "Q14344", "Q62132", "Q99677", "Q9UG90", "Q9BZH6", "Q9BQD3", "B4E025", "O60603", "Q01955", "Q92794", "Q5NHX7",
					"Q92791", "Q5NHX6", "Q5NI96", "Q5NHX9", "Q92793", "Q5NHX8", "Q5NHX3", "Q5NI93", "Q01959", "Q5NHX5", "P10644", "Q8WTV0",
					"Q6SA08", "C0LZJ3"*/}));

}

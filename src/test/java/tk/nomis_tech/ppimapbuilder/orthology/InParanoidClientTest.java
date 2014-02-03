package tk.nomis_tech.ppimapbuilder.orthology;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class InParanoidClientTest {

	@Test
	public void test() {
		ArrayList<Integer> orthotaxids = new ArrayList<Integer>() {{
			add(10090);
			add(3702);
			add(6239);
		}};

		ArrayList<String> ids = new ArrayList<String>() {{
			add("P07900");
			add("P04040");
			add("Q04565");
			add("Q9C519");
			add("P69891");
			add("P02091");
			add("P02008");
		}};
		
		for (Integer orthotaxid : orthotaxids) {
			System.out.println("--------[" + orthotaxid
					+ "]-----------------------------------");
			// tests
			for (String id : ids) {
				System.out.println("==" + id);
				InParanoidClient.getOrthologUniprotId(id, orthotaxid);
			}

		}
	}

}

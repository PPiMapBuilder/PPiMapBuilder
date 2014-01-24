package tk.nomis_tech.ppimapbuilder.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Box.Filler;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.GOCategory;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.GeneOntologyModel;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.UniProtProtein;

public class UniProtService {

	private static final String uniprotUrl = "http://www.uniprot.org/uniprot/";

	private UniProtService instance;

	public UniProtService getInstance() {
		if (instance == null)
			instance = new UniProtService();
		return instance;
	}

	private static UniProtProtein retrieveProteinData(String uniprotId) {

		Document doc = null;
		try {
			Connection conn = Jsoup.connect(uniprotUrl+uniprotId+".xml");
			doc = conn.get();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Integer taxId = null;
		String geneName = null;
		ArrayList<String> synonymGeneNames = new ArrayList<String>();
		String proteinName = null;
		boolean reviewed = false;
		
		// TAX ID
		for (Element e : doc.select("organism")) {
			taxId = Integer.valueOf(e.select("dbReference").attr("id")); // There is no problem, this is in the same way each time
			break;
		}
		
		// GENE NAME AND SYNONYMS
		for (Element e : doc.select("gene")) {
			for (Element f : e.select("name")) {
				if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
					geneName = f.text();
				}
				else { // Else, we store the names as synonyms
					synonymGeneNames.add(f.text());
				}
			}
		}
		
		// PROTEIN NAME
		for (Element e : doc.select("protein")) {
			if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
				proteinName = e.select("recommendedName").select("fullName").text();
			}
			else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exist, we take the submitted name (usually from TrEMBL but not always)
				proteinName = e.select("submittedName").select("fullName").text();
			}
			break;
		}
		
		// REVIEWED
		for (Element e : doc.select("entry")) {
			reviewed = e.attr("dataset").equalsIgnoreCase("Swiss-Prot")?true:false; // If the protein comes from Swiss-Prot, it is reviewed
			break;
		}
		
		// PROTEIN CREATION
		UniProtProtein prot = new UniProtProtein(uniprotId, geneName, taxId, proteinName, reviewed);
		prot.setSynonymGeneNames(synonymGeneNames);
		
		// GENE ONTOLOGIES
		for (Element e : doc.select("dbReference")) {
			if (e.attr("type").equals("GO")) {
				String id = e.attr("id");
				GOCategory category = null;
				String term = null;
				for (Element f: e.select("property")) {
					if (f.attr("type").equals("term")) {
						String value = f.attr("value");
						String[] values = value.split(":");
						if (values[0].equals("C")) {
							category = GOCategory.CELLULAR_COMPONENT;
						}
						else if (values[0].equals("F")) {
							category = GOCategory.MOLECULAR_FUNCTION;
						}
						else if (values[0].equals("P")) {
							category = GOCategory.BIOLOGICAL_PROCESS;
						}
						term = values[1];
						break;
					}
				}
				GeneOntologyModel go = new GeneOntologyModel(id, term, category);
				if (category == GOCategory.CELLULAR_COMPONENT) {
					prot.addCellularComponent(go);
				}
				else if (category == GOCategory.BIOLOGICAL_PROCESS) {
					prot.addBiologicalProcess(go);
				}
				else if (category == GOCategory.MOLECULAR_FUNCTION) {
					prot.addMolecularFunction(go);
				}
			}
		}
		
		return prot;
		
	}
	
	public static UniProtProtein getUniprotProtein(String uniprotid) {
		UniProtProtein prot = retrieveProteinData(uniprotid);
		return prot;
	}

}

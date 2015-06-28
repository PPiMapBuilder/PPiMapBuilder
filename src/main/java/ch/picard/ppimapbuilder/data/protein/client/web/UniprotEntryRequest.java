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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ch.picard.ppimapbuilder.util.concurrent.IteratorRequest;
import ch.picard.ppimapbuilder.util.io.IOUtils;
import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.UniProtEntry;

public class UniprotEntryRequest implements IteratorRequest<UniProtEntry>{

	private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";
	//Input
	final String originalUniProtId;

	//Output
	UniProtEntry protein = null;

	public UniprotEntryRequest(String originalUniProtId) {
		this.originalUniProtId = originalUniProtId;
	}

	@Override
	public Iterator<UniProtEntry> call() throws Exception {
		
		List<UniProtEntry> res = new ArrayList<UniProtEntry>();
		
		Document doc = IOUtils.getDocumentWithRetry(UNIPROT_URL + originalUniProtId + ".xml", 1200, 1000, 7, 100);

		if (doc == null || doc.select("body").get(0).childNodeSize() < 1)
			return res.iterator();

		UniProtEntry.Builder builder = new UniProtEntry.Builder();

		// ACCESSIONS
		boolean first = true;
		for (Element e : doc.select("accession")) {
			if (first) {
				builder.setUniprotId(e.text());
				first = false;
			}
			builder.addAccession(e.text());
		}
		builder.addAccession(originalUniProtId);

		// ORGANISM
		for (Element e : doc.select("organism")) {
			String scientificName = "";
			for (Element name : e.select("name")) {
				if (name.attr("type").equals("scientific")) {
					scientificName = name.text();
					break;
				}
			}
			builder.setOrganism(new Organism(
					scientificName,
					Integer.valueOf(e.select("dbReference").attr("id"))
			));
			break;
		}

		// GENE NAME AND SYNONYMS
		for (Element e : doc.select("gene")) {
			for (Element f : e.select("name")) {
				if (f.attr("type").equals("primary")) { // If the type is primary, this is the main name (sometimes there is no primary gene name :s)
					builder.setGeneName(f.text());
				} else { // Else, we organism the names as synonyms
					builder.addSynonymGeneName(f.text());
				}
			}
		}

		// PROTEIN NAME
		for (Element e : doc.select("protein")) {
			if (!e.select("recommendedName").isEmpty()) { // We retrieve the recommended name
				builder.setProteinName(e.select("recommendedName").select("fullName").text());
			} else if (!e.select("submittedName").isEmpty()) { // If the recommended name does not exists, we take the submitted name (usually from TrEMBL but not always)
				builder.setProteinName(e.select("submittedName").select("fullName").text());
			}
			break;
		}

		// REVIEWED
		for (Element e : doc.select("entry")) {
			builder.setReviewed(e.attr("dataset").equalsIgnoreCase("Swiss-Prot")); // If the protein comes from Swiss-Prot, it is reviewed
			break;
		}

		// EC NUMBER
		for (Element e : doc.select("ecNumber")) {
			builder.setEcNumber(e.text());
			break;
		}

		// GENE ONTOLOGIES
		for (Element e : doc.select("dbReference")) {
			if (e.attr("type").equals("GO")) {
				String id = e.attr("id");

				for (Element f : e.select("property")) {
					if (f.attr("type").equals("term")) {
						String[] values = f.attr("value").split(":");

						builder.addGeneOntologyTerm(new GeneOntologyTerm(id, values[1], values[0].charAt(0)));
						break;
					}
				}
			}
		}

		//System.out.println("uniprotEntryClient:"+protein.getUniProtId()+":"+pos+"try-ok");

		// PROTEIN CREATION
		protein = builder.build();
		res.add(protein);
		return res.iterator();
	}

}

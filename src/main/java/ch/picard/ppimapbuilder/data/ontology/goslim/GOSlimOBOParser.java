package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GOSlimOBOParser {

	public static GOSlim parseOBOFile(InputStream inputStream, String name) {
		final GOSlim set = new GOSlim(name);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			boolean inTerm = false;
			String identifier = null;
			String term = null;
			Character category = null;
			while((line = reader.readLine()) != null) {
				if(line.contains("[Term]"))
					inTerm = true;
				else if(inTerm && line.startsWith("id:")) {
					identifier = line.replace("id: ", "");
				}
				else if(line.startsWith("name:") && identifier != null) {
					term = line.replace("name: ", "");
				}
				else if(line.startsWith("namespace:") && term != null) {
					String namespace = line.replace("namespace: ", "");
					if(namespace.equals("biological_process"))
						category = 'P';
					else if(namespace.equals("molecular_function"))
						category = 'F';
					else if(namespace.equals("cellular_component"))
						category = 'C';

					set.add(new GeneOntologyTerm(identifier, term, category));
					inTerm = false;
					identifier = term = null;
					category = null;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}

		return set;
	}

}

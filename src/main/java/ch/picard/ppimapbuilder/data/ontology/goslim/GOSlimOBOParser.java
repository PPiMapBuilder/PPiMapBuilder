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
    
package ch.picard.ppimapbuilder.data.ontology.goslim;

import ch.picard.ppimapbuilder.data.ontology.GeneOntologyTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Rudimentary OBO parser that only read Gene Ontology Term from an OBO file to construct a GOSlim from these terms.
 */
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
				if(line.contains("[Term]")) {
					inTerm = true;
					identifier = term = null;
					category = null;
				}
				else if(inTerm && line.startsWith("id:")) {
					String id = line.replace("id: ", "");
					identifier = id.startsWith("GO") ? id : null;
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

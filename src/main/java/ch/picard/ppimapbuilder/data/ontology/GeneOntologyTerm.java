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
    
package ch.picard.ppimapbuilder.data.ontology;

import ch.picard.ppimapbuilder.data.JSONable;
import com.eclipsesource.json.JsonObject;

import java.io.Serializable;

public class GeneOntologyTerm extends OntologyTerm implements JSONable, Serializable {

	private static final long serialVersionUID = 1L;

	private final String term;
	private final GeneOntologyCategory category;
	public static final int TERM_LENGTH = 10;

	public GeneOntologyTerm(String identifier) {
		this(identifier, "", null);
	}

	public GeneOntologyTerm(String identifier, String term, char category) {
		this(identifier, term, GeneOntologyCategory.getByLetter(category));
	}

	public GeneOntologyTerm(String identifier, String term, GeneOntologyCategory category) {
		super(identifier);
		this.term = term;
		this.category = category;
	}

	public String getTerm() {
		return term;
	}

	public GeneOntologyCategory getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

	@Override
	public String toJSON() {
		JsonObject out = new JsonObject();
		out.add("id", getIdentifier());
		out.add("term", term);
		return out.toString();
	}
}

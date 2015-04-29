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
    
package ch.picard.ppimapbuilder.data.protein;

import ch.picard.ppimapbuilder.data.JSONable;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import com.eclipsesource.json.JsonObject;

import java.io.Serializable;

/**
 * Simple protein model with a unique identifier (UniProt identifier) and an Organism.
 */
public class Protein implements Serializable, JSONable {

	private static final long serialVersionUID = 1L;
	public static final int ID_LENGTH = 10; //MAX length of UniProt identifier

	protected final String uniProtId;
	protected final Organism organism;

	public Protein(Protein protein) {
		this(protein.getUniProtId(), protein.getOrganism());
	}

	public Protein(String uniProtId, Organism organism) {
		this.uniProtId = uniProtId;
		this.organism = organism;
	}

	public Protein(String uniProtId, int taxId) {
		this(uniProtId, UserOrganismRepository.getInstance().getOrganismByTaxId(taxId));
	}

	public String getUniProtId() {
		return uniProtId;
	}

	public Organism getOrganism() {
		return organism;
	}

	@Override
	public String toString() {
		return uniProtId;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Protein && uniProtId.equals(((Protein) o).uniProtId);
	}

	@Override
	public int hashCode() {
		return uniProtId != null ? uniProtId.hashCode() : super.hashCode();
	}

	@Override
	public String toJSON() {
		JsonObject out = new JsonObject();
		out.add("uniProtId", uniProtId);
		out.add("organism", organism.getTaxId());
		return out.toString();
	}
}

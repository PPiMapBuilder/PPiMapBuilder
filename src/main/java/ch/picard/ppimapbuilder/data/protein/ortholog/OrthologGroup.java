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
    
package ch.picard.ppimapbuilder.data.protein.ortholog;

import ch.picard.ppimapbuilder.data.Pair;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.protein.Protein;

import java.io.Serializable;
import java.util.*;

/**
 * Data structure representing a group of proteins orthologous with each other.
 * Inspired by the OrthoXML specifications (but not compliant with them).
 */
public class OrthologGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<Organism, List<OrthologScoredProtein>> members;

	/**
	 * Constructs an empty {@link OrthologGroup}
	 */
	public OrthologGroup() {
		this.members = new HashMap<Organism, List<OrthologScoredProtein>>();
	}

	/**
	 * Constructs a {@link OrthologGroup} filled with several
	 * orthologs as {@link OrthologScoredProtein}
	 */
	public OrthologGroup(OrthologScoredProtein... orthologScoredProteins) {
		this();
		for (OrthologScoredProtein orthologScoredProtein : orthologScoredProteins)
			add(orthologScoredProtein);
	}

	/**
	 * Adds an ortholog to the group using a {@link OrthologScoredProtein}
	 */
	public void add(OrthologScoredProtein protein) {
		List<OrthologScoredProtein> proteins = members.get(protein.getOrganism());
		if (proteins == null)
			members.put(protein.getOrganism(), (proteins = new ArrayList<OrthologScoredProtein>()));
		proteins.add(protein);
	}

	/**
	 * Adds an ortholog to the group using a {@link Protein} with
	 * a orthology score
	 */
	public void add(Protein protein, Double score) {
		add(new OrthologScoredProtein(protein, score));
	}

	/**
	 * Gets the orthologs with the best score in the given {@link Organism}.
	 */
	public List<OrthologScoredProtein> getBestOrthologsInOrganism(Organism organism) {
		List<OrthologScoredProtein> bestOrthologs = new ArrayList<OrthologScoredProtein>();
		List<OrthologScoredProtein> proteins = members.get(organism);

		if (proteins != null && proteins.size() > 0) {
			Collections.sort(proteins, new Comparator<OrthologScoredProtein>() {
				@Override
				public int compare(OrthologScoredProtein o1, OrthologScoredProtein o2) {
					return o2.getScore().compareTo(o1.getScore());
				}
			});

			double bestScore = proteins.get(0).getScore();
			for(OrthologScoredProtein protein : proteins) {
				if(protein.getScore() == bestScore)
					bestOrthologs.add(protein);
				else break;
			}
		}

		return bestOrthologs;
	}

	/**
	 * An {@link OrthologGroup} is considered valid if it has at least two protein from different organisms.
	 */
	public boolean isValid() {
		return members.keySet().size() >= 2;
	}

	/**
	 * Gets the organisms of the orthologs in the group.
	 */
	public List<Organism> getOrganisms() {
		return new ArrayList<Organism>(members.keySet());
	}

	/**
	 * Gets all proteins in the group.
	 */
	public List<OrthologScoredProtein> getProteins() {
		ArrayList<OrthologScoredProtein> result = new ArrayList<OrthologScoredProtein>();

		for (List<OrthologScoredProtein> value : members.values())
			result.addAll(value);

		return result;
	}

	/**
	 * Checks if the group has at least one protein with the given organism.
	 */
	public boolean contains(Organism organism) {
		return members.keySet().contains(organism);
	}

	/**
	 * Checks if the group contains the given protein.
	 */
	public boolean contains(Protein protein) {
		return getProteins().contains(protein);
	}

	public OrthologScoredProtein find(Protein protein) {
		for (OrthologScoredProtein orthologScoredProtein : members.get(protein.getOrganism()))
			if (orthologScoredProtein.getUniProtId().equals(protein.getUniProtId()))
				return orthologScoredProtein;
		return null;
	}

}

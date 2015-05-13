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
    
package ch.picard.ppimapbuilder.util.test;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;

import java.util.List;

public class DummyNetworkQueryParameters implements NetworkQueryParameters {
	private final List<PsicquicService> selectedDatabases;
	private final List<String> proteinOfInterestUniprotId;
	private final Organism referenceOrganism;
	private final List<Organism> otherOrganisms;
	private final boolean interactomeQuery;

	public DummyNetworkQueryParameters(
			List<PsicquicService> selectedDatabases,
			List<String> proteinOfInterestUniprotId,
			Organism referenceOrganism,
			List<Organism> otherOrganisms,
			boolean interactomeQuery) {
		this.selectedDatabases = selectedDatabases;
		this.proteinOfInterestUniprotId = proteinOfInterestUniprotId;
		this.referenceOrganism = referenceOrganism;
		this.otherOrganisms = otherOrganisms;
		this.interactomeQuery = interactomeQuery;
	}

	@Override
	public List<String> getProteinOfInterestUniprotId() {
		return proteinOfInterestUniprotId;
	}

	@Override
	public Organism getReferenceOrganism() {
		return referenceOrganism;
	}

	@Override
	public List<Organism> getOtherOrganisms() {
		return otherOrganisms;
	}

	@Override
	public List<PsicquicService> getSelectedDatabases() {
		return selectedDatabases;
	}

	@Override
	public boolean isInteractomeQuery() {
		return interactomeQuery;
	}
}

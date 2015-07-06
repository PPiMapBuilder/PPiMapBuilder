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

package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import com.google.common.collect.Sets;
import psidev.psi.mi.tab.model.Interactor;

import java.util.Collection;
import java.util.Set;

public class OrganismInteractorFilter extends InteractorFilter {
	private final Set<Organism> organism;

	public OrganismInteractorFilter(Organism... organisms) {
		this.organism = Sets.newHashSet(organisms);
	}

	public OrganismInteractorFilter(Collection<Organism> organisms) {
		this.organism = Sets.newHashSet(organisms);
	}

	@Override
	public boolean isValidInteractor(Interactor interactor) {
		try {
			return organism.contains(
					OrganismUtils.findOrganismInMITABTaxId(
							InParanoidOrganismRepository.getInstance(),
							interactor.getOrganism().getTaxid()
					)
			);
		} catch (Exception e) {
			return false;
		}
	}
}

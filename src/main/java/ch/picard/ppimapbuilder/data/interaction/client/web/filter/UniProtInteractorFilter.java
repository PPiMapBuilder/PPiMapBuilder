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

import ch.picard.ppimapbuilder.data.protein.ProteinUtils;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;

import java.util.ArrayList;
import java.util.List;

public final class UniProtInteractorFilter extends InteractorFilter {
	@Override
	public boolean isValidInteractor(Interactor interactor) {
		final List<CrossReference> ids = interactor.getIdentifiers();
		ids.addAll(interactor.getAlternativeIdentifiers());

		if (ids.size() == 1 && !ids.get(0).getDatabase().equals("uniprotkb"))
			return false;

		CrossReference uniprot = null;
		boolean hasUniprot = false;
		for (CrossReference ref : ids) {
			hasUniprot = hasUniprot || (
					ref.getDatabase().equals("uniprotkb") // Is UniProt
							&&
							ProteinUtils.UniProtId.isValid(ref.getIdentifier()) // Valid UniProt
			);
			if (hasUniprot) {
				uniprot = ref;
				break;
			}
		}

		if (!hasUniprot)
			return false;

		List<CrossReference> sortedIdentifiers = new ArrayList<CrossReference>();
		ids.remove(uniprot);
		sortedIdentifiers.add(uniprot);
		sortedIdentifiers.addAll(ids);
		interactor.setIdentifiers(sortedIdentifiers);

		return true;
	}
}

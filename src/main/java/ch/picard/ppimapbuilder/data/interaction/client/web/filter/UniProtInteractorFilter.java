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

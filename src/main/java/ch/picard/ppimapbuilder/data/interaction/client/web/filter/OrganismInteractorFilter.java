package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import ch.picard.ppimapbuilder.data.organism.InParanoidOrganismRepository;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.data.organism.OrganismUtils;
import psidev.psi.mi.tab.model.Interactor;

public final class OrganismInteractorFilter extends InteractorFilter {
	private final Organism organism;

	public OrganismInteractorFilter(Organism organism) {
		this.organism = organism;
	}

	@Override
	public boolean isValidInteractor(Interactor interactor) {
		try {
			return organism.equals(
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

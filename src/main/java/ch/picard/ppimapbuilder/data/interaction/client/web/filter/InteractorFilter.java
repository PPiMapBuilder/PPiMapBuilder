package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

public abstract class InteractorFilter extends InteractionFilter {
	public abstract boolean isValidInteractor(Interactor interactor);

	@Override
	public boolean isValidInteraction(BinaryInteraction interaction) {
		if(interaction == null) return false;
		Interactor interactorA = interaction.getInteractorA();
		Interactor interactorB = interaction.getInteractorB();
		return isValidInteractor(interactorA)
				&& isValidInteractor(interactorB);
	}
}

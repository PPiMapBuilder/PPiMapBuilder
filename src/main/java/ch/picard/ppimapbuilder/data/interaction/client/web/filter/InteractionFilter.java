package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import com.google.common.base.Predicate;
import psidev.psi.mi.tab.model.BinaryInteraction;

public abstract class InteractionFilter implements Predicate<BinaryInteraction> {
	public abstract boolean isValidInteraction(BinaryInteraction interaction);

	@Override
	public boolean apply(BinaryInteraction binaryInteraction) {
		return isValidInteraction(binaryInteraction);
	}
}

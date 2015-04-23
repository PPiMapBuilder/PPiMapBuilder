package ch.picard.ppimapbuilder.data.interaction.client.web.filter;

import ch.picard.ppimapbuilder.util.ProgressMonitor;
import psidev.psi.mi.tab.model.BinaryInteraction;

public class ProgressMonitoringInteractionFilter extends InteractionFilter {
	private final int estimatedNumberOfInteraction;
	private final ProgressMonitor progressMonitor;
	private double interactionCount;
	private double percent;

	public ProgressMonitoringInteractionFilter(int estimatedNumberOfInteraction, ProgressMonitor progressMonitor) {
		this.estimatedNumberOfInteraction = estimatedNumberOfInteraction;
		this.progressMonitor = progressMonitor;
		interactionCount = 0;
		percent = 0;
	}

	@Override
	public boolean isValidInteraction(BinaryInteraction interaction) {
		double p = Math.floor(++interactionCount / estimatedNumberOfInteraction * 100) / 100;
		if(p > percent) {
			progressMonitor.setProgress(percent = p);
		}
		return true;
	}
}

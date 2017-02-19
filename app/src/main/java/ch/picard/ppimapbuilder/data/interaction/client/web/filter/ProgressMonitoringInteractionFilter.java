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
		if(p > percent && p < 1) {
			progressMonitor.setProgress(percent = p);
		}
		return true;
	}
}

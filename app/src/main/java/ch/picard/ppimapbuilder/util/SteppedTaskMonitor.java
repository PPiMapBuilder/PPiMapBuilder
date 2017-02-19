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
    
package ch.picard.ppimapbuilder.util;

import org.cytoscape.work.TaskMonitor;

/**
 * Simple Task implementation that tracks progress using a fixed number of steps
 */
public class SteppedTaskMonitor implements TaskMonitor {

	private final double nbStep;
	private int currentStep;
	private TaskMonitor monitor;


	public SteppedTaskMonitor(TaskMonitor monitor, final double nbStep) {
		this.monitor = monitor;
		this.nbStep = nbStep;
		this.currentStep = -1;
	}

	public void setStep(String message) {
		setStatusMessage(message);
		setProgress(++currentStep / nbStep);
	}

	@Override
	public void setTitle(String s) {
		if (monitor != null) {
			monitor.setTitle(s);
		}
		System.out.println("[TITLE]\t\t" + s);

	}

	@Override
	public void setProgress(double v) {
		if (monitor != null) {
			monitor.setProgress(v);
		}
		System.out.println("[PROGRESS]\t" + (int) (v * 100.0) + "%");
	}

	@Override
	public void setStatusMessage(String s) {
		if (monitor != null) {
			monitor.setStatusMessage(s);
		}
		System.out.println("[STATUS]\t" + s);

	}
}

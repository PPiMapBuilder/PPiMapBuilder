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

import ch.picard.ppimapbuilder.util.ProgressMonitor;
import org.cytoscape.work.TaskMonitor;

public class DummyTaskMonitor implements TaskMonitor, ProgressMonitor {

	@Override
	public void setTitle(String s) {
		System.out.println("[TITLE]\t\t" + s);
	}

	@Override
	public void setProgress(double v) {
		System.out.println("[PROGRESS]\t" + (int) (v * 100.0) + "%");
	}

	@Override
	public void setStatusMessage(String s) {
		System.out.println("[STATUS]\t" + s);
	}

}

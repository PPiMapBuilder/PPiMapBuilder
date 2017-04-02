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
    
package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.ui.settingwindow.SettingWindow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBSettingMenuTaskFactory extends AbstractTaskFactory {

	private final SettingWindow settingWindow;

	public PMBSettingMenuTaskFactory(SettingWindow settingWindow) {
		this.settingWindow = settingWindow;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
			new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							settingWindow.setVisible(true);
						}
					});
				}
			}
		);
	}

}

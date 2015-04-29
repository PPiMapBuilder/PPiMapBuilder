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

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicRegistry;
import ch.picard.ppimapbuilder.data.organism.UserOrganismRepository;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import ch.picard.ppimapbuilder.ui.querywindow.QueryWindow;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import java.io.IOException;

/**
 * PPiMapBuilder app sub menu
 */
public class PMBQueryMenuTaskFactory extends AbstractTaskFactory {

	private final QueryWindow queryWindow;

	public PMBQueryMenuTaskFactory(QueryWindow queryWindow) {
		this.queryWindow = queryWindow;
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
							try {
								PsicquicRegistry reg = PsicquicRegistry.getInstance();
								queryWindow.updateLists(
										reg.getServices(),
										UserOrganismRepository.getInstance().getOrganisms()
								);

								queryWindow.setVisible(true);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(null, "Unable to get PSICQUIC databases");
							}
						}
					});
				}
			}
		);
	}

}

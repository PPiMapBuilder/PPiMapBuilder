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
    
package ch.picard.ppimapbuilder.data.protein.ortholog.client.cache.loader;

import ch.picard.ppimapbuilder.data.organism.Organism;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

import java.util.List;

public class InParanoidCacheLoaderTaskFactory extends AbstractTaskFactory {
	private List<Organism> organisms;
	private Task callback;
	private String message;
	private String error;

	@Override
	public TaskIterator createTaskIterator() {
		message = null;
		error = null;
		return new TaskIterator(
				new InParanoidCacheLoaderTask(this, callback)
		);
	}

	public void setOrganisms(List<Organism> organisms) {
		this.organisms = organisms;
	}

	public List<Organism> getOrganisms() {
		return organisms;
	}

	public void setCallback(Task callback) {
		this.callback = callback;
	}

	public Task getCallback() {
		return callback;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}
}

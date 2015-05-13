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
    
package ch.picard.ppimapbuilder.data.ontology.goslim;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.work.*;

import java.io.File;
import java.io.FileInputStream;

public class GOSlimLoaderTaskFactory extends AbstractTaskFactory {

	private String error;
	private Task callback;

	public GOSlimLoaderTaskFactory(Task callback) {
		this.callback = callback;
	}

	public GOSlimLoaderTaskFactory() {}

	public void setCallback(Task callback) {
		this.callback = callback;
	}

	@Override
	public TaskIterator createTaskIterator() {
		error = null;
		final TaskIterator taskIterator = new TaskIterator(
				new LoadGOSlimTask()
		);
		if (callback != null)
			taskIterator.append(callback);

		return taskIterator;
	}

	public class LoadGOSlimTask extends AbstractTask {

		private String name;

		@Tunable(description = "Name of the GO slim", listenForChange = "OBO file")
		public String getName() {
			if (file != null && file.isFile() && name.equals(""))
				return file.getName();
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		private File file;

		@Tunable(description = "OBO file", params = "input=true")
		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		@ProvidesTitle
		public String getTitle() {
			return "Add a new GO slim";
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			if (name == null || name.isEmpty()) {
				error = "Please enter a GO slim name.";
				return;
			}
			if (name.equals(GOSlim.DEFAULT)) {
				error = "The GO slim name '" + name + "' is already taken. Please choose an other one";
				return;
			}
			if (file == null) {
				error = "Please select an OBO file to add a new GO slim.";
				return;
			}
			if (!file.isFile() || !FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("obo")) {
				error = "You selected a wrong format file. Please select an OBO file to add a new GO slim.";
				return;
			}

			GOSlim goSlim = GOSlimOBOParser.parseOBOFile(new FileInputStream(file), name);

			if (goSlim == null || goSlim.isEmpty()) {
				error = "GO slim could not be read. Please check your OBO file is readable.";
				return;
			}

			GOSlimRepository.getInstance().addGOSlim(goSlim);
		}

		@Override
		public void cancel() {
			super.cancel();
			try {
				GOSlimLoaderTaskFactory.this.callback.run(null);
			} catch (Exception e) {}
		}

	}

	public String getError() {
		return error;
	}
}


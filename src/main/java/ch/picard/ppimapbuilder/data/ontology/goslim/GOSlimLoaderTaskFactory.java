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


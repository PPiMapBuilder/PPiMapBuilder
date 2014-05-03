package tk.nomis_tech.ppimapbuilder;

import tk.nomis_tech.ppimapbuilder.util.FileUtil;

import java.io.File;

public class TestUtils {
	public static void recursiveDelete(File file) {
		FileUtil.recursiveDelete(file);
	}

	public static File createTestOutPutFolder(String name, boolean deleteAtShutDown) {
		final File testFolderOutput = new File(name + "-output-" + System.currentTimeMillis());
		testFolderOutput.mkdir();

		if(deleteAtShutDown) {
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					FileUtil.recursiveDelete(testFolderOutput);
				}
			});
		}

		return testFolderOutput;
	}
}

package tk.nomis_tech.ppimapbuilder;

import java.io.File;
import java.io.IOException;

public class TestUtils {
	public static void recursiveDelete(File file) {
		if (!file.exists())
			return;
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				recursiveDelete(f);
			}
		}
		file.delete();
	}

	public static File createTestOutPutFolder() {
		try {
			File testFolderOutput = File.createTempFile("test-output", "", new File("."));
			testFolderOutput.delete();
			testFolderOutput.mkdir();
			return testFolderOutput;
		} catch (IOException e) {
			return null;
		}
	}
}

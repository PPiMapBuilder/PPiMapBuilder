package tk.nomis_tech.ppimapbuilder;

import tk.nomis_tech.ppimapbuilder.util.FileUtil;

import java.io.File;
import java.io.IOException;

public class TestUtils {
	public static void recursiveDelete(File file) {
		FileUtil.recursiveDelete(file);
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

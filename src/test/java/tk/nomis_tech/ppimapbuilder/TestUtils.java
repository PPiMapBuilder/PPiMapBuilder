package tk.nomis_tech.ppimapbuilder;

import tk.nomis_tech.ppimapbuilder.util.FileUtil;

import java.io.File;

public class TestUtils {
	public static void recursiveDelete(File file) {
		FileUtil.recursiveDelete(file);
	}

	public static File createTestOutPutFolder() {
		File testFolderOutput = new File("test-output" + System.currentTimeMillis());
		testFolderOutput.mkdir();
		return testFolderOutput;
	}
}

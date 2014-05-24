package tk.nomis_tech.ppimapbuilder.util;

import junit.framework.TestCase;
import tk.nomis_tech.ppimapbuilder.TestUtils;

import java.io.File;

public class FileUtilTest extends TestCase {

	public void testGetHumanReadableFileSize() throws Exception {
		File folder = TestUtils.getBaseTestOutputFolder();

		String size = FileUtil.getHumanReadableFileSize(folder);

		System.out.println(size);
	}
}
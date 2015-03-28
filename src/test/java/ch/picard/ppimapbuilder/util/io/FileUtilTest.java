package ch.picard.ppimapbuilder.util.io;

import ch.picard.ppimapbuilder.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;

public class FileUtilTest extends TestCase {

	@Test
	public void testGetHumanReadableFileSize() throws Exception {
		File folder = TestUtils.getBaseTestOutputFolder();

		String size = FileUtil.getHumanReadableFileSize(folder);

		System.out.println(size);
	}
}
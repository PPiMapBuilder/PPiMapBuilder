package ch.picard.ppimapbuilder.util.io;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class FileUtil {
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

	public static String getHumanReadableFileSize(File file) {
		return FileUtils.byteCountToDisplaySize(
				file.exists() ?
						getFileSizeRecursive(file) :
						0
		);
	}

	private static long getFileSizeRecursive(File file) {
		if (file == null || !file.exists())
			return 0l;
		else {
			if (file.isDirectory()) {
				long cumul = 0l;
				for (File f : file.listFiles()) {
					cumul += getFileSizeRecursive(f);
				}
				return cumul;
			} else {
				return file.length();
			}
		}
	}
}

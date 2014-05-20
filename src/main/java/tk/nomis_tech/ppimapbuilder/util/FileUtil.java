package tk.nomis_tech.ppimapbuilder.util;

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
		if(!file.exists())
			return "-";
		else
			return FileUtils.byteCountToDisplaySize(getFileSizeRecursive(file));
	}

	private static long getFileSizeRecursive(File file) {
		if(file == null || !file.exists())
			return 0l;
		else {
			if(file.isDirectory()) {
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

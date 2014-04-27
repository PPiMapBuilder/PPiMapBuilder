package tk.nomis_tech.ppimapbuilder.util;

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
}

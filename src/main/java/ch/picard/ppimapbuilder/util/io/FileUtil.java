/*
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 *
 */

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

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
    
package ch.picard.ppimapbuilder;

import ch.picard.ppimapbuilder.util.FileUtil;

import java.io.File;

public class TestUtils {

	private static final boolean deleteAtShutDown = false;
	private static final File baseTestOutputFolder = new File("test-output");

	public static File createTestOutputFolder(String preffix) {
		final File testFolderOutput = getTestOutputFolder(preffix + "-output-" + System.currentTimeMillis());
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

	public static File getBaseTestOutputFolder() {
		return baseTestOutputFolder;
	}

	public static File getTestOutputFolder(String folderName) {
		return new File(baseTestOutputFolder, folderName);
	}
}

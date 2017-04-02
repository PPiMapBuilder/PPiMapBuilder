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
    
package ch.picard.ppimapbuilder.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSONUtils {

	public static List<String> jsonListToStringList(JSONable... jsonAbles) {
		ArrayList<String> list = new ArrayList<String>();
		for (JSONable jsonAble : jsonAbles) {
			list.add(jsonAble.toJSON());
		}
		return list;
	}

	public static List<String> jsonListToStringList(Collection<? extends JSONable> jsonAbles) {
		return jsonListToStringList(jsonAbles.toArray(new JSONable[jsonAbles.size()]));
	}

}

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
    
package ch.picard.ppimapbuilder.util.test;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import java.util.List;
import java.util.Map;

public class DummyCyRow implements CyRow {
	@Override
	public <T> T get(String s, Class<? extends T> aClass) {
		return null;
	}

	@Override
	public <T> T get(String s, Class<? extends T> aClass, T t) {
		return null;
	}

	@Override
	public <T> List<T> getList(String s, Class<T> tClass) {
		return null;
	}

	@Override
	public <T> List<T> getList(String s, Class<T> tClass, List<T> ts) {
		return null;
	}

	@Override
	public <T> void set(String s, T t) {

	}

	@Override
	public boolean isSet(String s) {
		return false;
	}

	@Override
	public Map<String, Object> getAllValues() {
		return null;
	}

	@Override
	public Object getRaw(String s) {
		return null;
	}

	@Override
	public CyTable getTable() {
		return null;
	}
}

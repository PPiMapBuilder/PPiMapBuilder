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
    
package ch.picard.ppimapbuilder.data.interaction.client.web.miql;

import java.util.ArrayList;
import java.util.List;

public class AbstractMiQLQueryElement {
	protected final List<Object> exprs = new ArrayList<Object>();
	private String cache = null;
	protected String separator = "";
	
	public AbstractMiQLQueryElement() {}
	
	public AbstractMiQLQueryElement(String elem) {
		add(elem);
	}
	
	public AbstractMiQLQueryElement(AbstractMiQLQueryElement elem) {
		add(elem);
	}

	public void add(AbstractMiQLQueryElement elem) {
		exprs.add(elem);
		cache = null;
	}
	
	public void add(String elem) {
		if(elem.contains(" "))
			exprs.add(new StringBuilder("\"").append(elem).append("\""));
		else exprs.add(elem);
		cache = null;
	}

	public void addAll(List<String> elements) {
		for (String elem : elements) {
			add(elem);
		}
	}
	
	public void addAllElement(List<AbstractMiQLQueryElement> elements) {
		exprs.addAll(elements);
	}
	
	public int size() {
		return exprs.size();
	}
	
	@Override
	public String toString() {
		if(cache == null) {
			StringBuilder out = new StringBuilder();
			
			boolean first = true;
			for(Object expr: exprs) {
				if(!first) out.append(separator);
				out.append(expr.toString());
				first = false;
			}
			
			cache = out.toString();
		}
		return cache;
	}
}

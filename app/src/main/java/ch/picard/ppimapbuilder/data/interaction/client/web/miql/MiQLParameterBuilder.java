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

/**
 * Representation of a MiQL parameter<br/>
 * Example result:
 * <ul>
 * <li>"name:value"</li>
 * <li>"value"</li>
 * </ul>
 */
public class MiQLParameterBuilder extends AbstractMiQLQueryElement {

	public MiQLParameterBuilder(AbstractMiQLQueryElement value) {
		super(value);
	}
	
	public MiQLParameterBuilder(String value) {
		super(value);
	}
	
	public MiQLParameterBuilder(String name, String value) {
		this(name, (Object)value);
	}
	
	public MiQLParameterBuilder(String name, AbstractMiQLQueryElement value) {
		this(name, (Object)value);
	}
	
	public MiQLParameterBuilder(String name, Object value) {
		super(name+":");
		
		if(value instanceof AbstractMiQLQueryElement) add((AbstractMiQLQueryElement) value);
		else add(value.toString());
	}	
}
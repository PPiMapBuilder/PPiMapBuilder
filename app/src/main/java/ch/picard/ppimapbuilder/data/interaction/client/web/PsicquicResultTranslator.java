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
    
package ch.picard.ppimapbuilder.data.interaction.client.web;

import psidev.psi.mi.tab.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PsicquicResultTranslator {

	public static <T> List<String> convert(Collection<T> list) {
		List<String> retList = new ArrayList<String>();

		if (!list.isEmpty()) {

			for (T elt : list) {

				if (elt instanceof Author) {
					retList.add(((Author) elt).getName());

				} else if (elt instanceof CrossReference) {				
					try { // We first try to use human-readable value (for source database e.g.)
						retList.add(((CrossReference) elt).getText().toString());
					}
					catch (Exception e) { // If there is no such value (for publications e.g.), we take the psi-mi ID
						retList.add(((CrossReference) elt).getDatabase() + ":" + ((CrossReference) elt).getIdentifier());
					}

				} else if (elt instanceof Alias) {
					retList.add(((Alias) elt).getDbSource() + ":" + ((Alias) elt).getName());

				} else if (elt instanceof Annotation) {
					retList.add(((Annotation) elt).getText());

				} else if (elt instanceof Checksum) {
					retList.add(((Checksum) elt).getChecksum());

				} else if (elt instanceof Confidence) {
					retList.add(((Confidence) elt).getType() + ":" + ((Confidence) elt).getValue());

				} else if (elt instanceof Feature) {
					retList.add(((Feature) elt).getFeatureType() + ":" + ((Feature) elt).getText());

				} else if (elt instanceof Organism) {
					retList.add(((Organism) elt).getTaxid());

				} else if (elt instanceof Parameter) {
					retList.add(((Parameter) elt).getType() + ":" + ((Parameter) elt).getValue() + "." + ((Parameter) elt).getUnit() + ":");
				}
			}
		}

		return retList;

	}
}

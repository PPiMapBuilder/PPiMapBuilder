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
					retList.add(((CrossReference) elt).getDatabase() + ":" + ((CrossReference) elt).getIdentifier());

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

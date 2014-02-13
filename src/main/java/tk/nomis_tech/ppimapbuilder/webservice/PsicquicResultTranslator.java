package tk.nomis_tech.ppimapbuilder.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.Annotation;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.Checksum;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Feature;
import psidev.psi.mi.tab.model.Parameter;
import tk.nomis_tech.ppimapbuilder.util.Organism;

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
					retList.add(((Organism) elt).getName());

				} else if (elt instanceof Parameter) {
					retList.add(((Parameter) elt).getType() + ":" + ((Parameter) elt).getValue() + "." + ((Parameter) elt).getUnit() + ":");
				}
			}
		}

		return retList;

	}
}

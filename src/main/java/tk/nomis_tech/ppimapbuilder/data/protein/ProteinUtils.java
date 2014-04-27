package tk.nomis_tech.ppimapbuilder.data.protein;

import tk.nomis_tech.ppimapbuilder.data.organism.Organism;

import java.util.ArrayList;
import java.util.Collection;

public class ProteinUtils {

	public static Collection<Protein> newProteins(final Collection<String> identifiers, final Organism organism) {
		return new ArrayList<Protein>(){{
			for(String id : identifiers)
				add(new Protein(id, organism));
		}};
	}

	public static Collection<String> asIdentifiers(final Collection<Protein> proteins) {
		return new ArrayList<String>() {{
			for(Protein protein: proteins)
				add(protein.getUniProtId());
		}};
	}
}

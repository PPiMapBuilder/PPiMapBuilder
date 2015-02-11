package ch.picard.ppimapbuilder.networkbuilder;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;

import java.util.List;
import java.util.Set;

public interface NetworkQueryParameters {

	public List<String> getProteinOfInterestUniprotId();

	public Organism getReferenceOrganism();

	public List<Organism> getOtherOrganisms();

	public List<PsicquicService> getSelectedDatabases();

	public boolean isInteractomeQuery();

}

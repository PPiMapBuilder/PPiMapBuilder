package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import java.util.List;
import java.util.Map;

public interface NetworkQueryPanel extends NetworkQueryParameters, TabContent {

	void updateDatabases(List<Map> databases);

	void updateOrganisms(List<Organism> organisms);

}

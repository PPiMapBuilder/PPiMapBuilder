package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabContent;

import javax.swing.*;
import java.util.List;

public interface NetworkQueryPanel extends NetworkQueryParameters, TabContent {

	public void updateLists(List<PsicquicService> databases, List<Organism> organisms);

}

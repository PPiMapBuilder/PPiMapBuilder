package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.DatabaseSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.OtherOrganismSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.ReferenceOrganismSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.UniprotSelection;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProteinNetworkQueryPanel extends CustomSplitPane implements NetworkQueryPanel {

	private final UniprotSelection uniprotSelection;
	private final DatabaseSelectionPanel databaseSelectionPanel;
	private final OtherOrganismSelectionPanel otherOrganismSelectionPanel;
	private final ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel;

	public ProteinNetworkQueryPanel() {
		super(PMBUIStyle.focusActiveTabColor, PMBUIStyle.blurActiveTabColor);
		setName("Protein Network");

		// Left panel
		setLeftComponent(
				uniprotSelection = new UniprotSelection()
		);

		{// Right panel
			otherOrganismSelectionPanel = new OtherOrganismSelectionPanel();
			referenceOrganismSelectionPanel = new ReferenceOrganismSelectionPanel(otherOrganismSelectionPanel);

			databaseSelectionPanel = new DatabaseSelectionPanel();

			setRightComponent(initRightPanel());
		}
	}

	private JPanel initRightPanel() {
		JPanel rightPanel = new JPanel();

		rightPanel.setBorder(PMBUIStyle.fancyPanelBorder);
		rightPanel.setMinimumSize(new Dimension(440, 10));
		rightPanel.setLayout(new MigLayout("inset 15", "[grow]", "[][50%][50%]"));
		rightPanel.add(referenceOrganismSelectionPanel, "growx");
		rightPanel.add(otherOrganismSelectionPanel, "newline, grow");
		rightPanel.add(databaseSelectionPanel, "newline, grow");

		return rightPanel;
	}

	@Override
	public List<String> getProteinOfInterestUniprotId() {
		return uniprotSelection.getIdentifiers();
	}

	@Override
	public Organism getReferenceOrganism() {
		return referenceOrganismSelectionPanel.getSelectedOrganism();
	}

	@Override
	public List<Organism> getOtherOrganisms() {
		return otherOrganismSelectionPanel.getSelectedOrganisms();
	}

	@Override
	public List<PsicquicService> getSelectedDatabases() {
		return databaseSelectionPanel.getSelectedDatabases();
	}

	@Override
	public boolean isInteractomeQuery() {
		return false;
	}

	@Override
	public void updateLists(List<PsicquicService> databases, List<Organism> organisms) {
		otherOrganismSelectionPanel.updateList(organisms);
		referenceOrganismSelectionPanel.updateList(organisms);
		databaseSelectionPanel.updateList(databases);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setActive(boolean active) {}

}

package ch.picard.ppimapbuilder.ui.querywindow.component.panel;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.DatabaseSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.field.ReferenceOrganismSelectionPanel;
import ch.picard.ppimapbuilder.ui.util.FocusPropagator;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

public class InteractomeNetworkQueryPanel extends JPanel implements NetworkQueryPanel, FocusListener {

	private final ReferenceOrganismSelectionPanel referenceOrganismSelectionPanel;
	private final DatabaseSelectionPanel databaseSelectionPanel;
	private final Border focusBorder = new CompoundBorder(
			new MatteBorder(5, 5, 5, 5, PMBUIStyle.focusActiveTabColor),
			PMBUIStyle.fancyPanelBorder
	);
	private final Border blurBorder = new CompoundBorder(
			new MatteBorder(5, 5, 5, 5, PMBUIStyle.blurActiveTabColor),
			PMBUIStyle.fancyPanelBorder
	);

	public InteractomeNetworkQueryPanel() {
		setName("Interactome Network");
		setLayout(new MigLayout("ins 15", "[grow]", "[][grow]"));

		setBorder(focusBorder);

		add(referenceOrganismSelectionPanel = new ReferenceOrganismSelectionPanel(), "growx");
		add(databaseSelectionPanel = new DatabaseSelectionPanel(), "newline, grow");
	}

	@Override
	public List<String> getProteinOfInterestUniprotId() {
		return null;
	}

	@Override
	public Organism getReferenceOrganism() {
		return referenceOrganismSelectionPanel.getSelectedOrganism();
	}

	@Override
	public List<Organism> getOtherOrganisms() {
		return null;
	}

	@Override
	public List<PsicquicService> getSelectedDatabases() {
		return databaseSelectionPanel.getSelectedDatabases();
	}

	@Override
	public void updateLists(List<PsicquicService> databases, List<Organism> organisms) {
		referenceOrganismSelectionPanel.updateList(organisms);
		databaseSelectionPanel.updateList(databases);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setActive(boolean active) {}

	@Override
	public void focusGained(FocusEvent e) {
		setBorder(focusBorder);
	}

	@Override
	public void focusLost(FocusEvent e) {
		setBorder(blurBorder);
	}
}

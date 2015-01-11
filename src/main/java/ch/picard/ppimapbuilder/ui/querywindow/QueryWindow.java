package ch.picard.ppimapbuilder.ui.querywindow;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.InteractomeNetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.NetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.ProteinNetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.focus.FocusPropagatorListener;
import ch.picard.ppimapbuilder.ui.util.focus.FocusPropagator;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabPanel;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame implements NetworkQueryParameters, FocusPropagatorListener {

	/**
	 * Instance of the PPiMapBuilder frame to prevent several instances
	 */
	private static final long serialVersionUID = 1L;

	// Task management
	private PMBInteractionNetworkBuildTaskFactory createNetworkFactory;
	private TaskManager taskManager;

	// Network query panels
	private final TabPanel<NetworkQueryPanel> networkQueryPanels;

	// Bottom panel
	private final JPanel bottomPanel;
	private JButton submitButton;
	private JButton cancelButton;

	/**
	 * Create the network creation frame
	 */
	public QueryWindow() {
		super("PPiMapBuilder - Create a network");

		// Focus / Blur listener for component color change
		FocusPropagator focusPropagator = new FocusPropagator(this);
		this.addWindowFocusListener(focusPropagator);

		{ // Init components
			this.setLayout(new BorderLayout());

			// Tab panel with protein and interactome network query panels
			networkQueryPanels = new TabPanel<NetworkQueryPanel>(
					focusPropagator,
					new ProteinNetworkQueryPanel(focusPropagator),
					new InteractomeNetworkQueryPanel(focusPropagator)
			);
			this.add(networkQueryPanels, BorderLayout.CENTER);

			// Bottom panel
			bottomPanel = new JPanel();
			{
				bottomPanel.setBackground(PMBUIStyle.focusActiveTabColor);
				bottomPanel.setLayout(new MigLayout("inset 5", "[grow][][]", "[29px]"));
				bottomPanel.setPreferredSize(new Dimension(0, 42));

				//Cancel Button
				cancelButton = new JButton("Cancel");
				cancelButton.setMnemonic(KeyEvent.VK_CANCEL);
				bottomPanel.add(cancelButton, "skip, alignx center, aligny center");

				//Submit Button
				submitButton = new JButton("Submit");
				bottomPanel.add(submitButton, "alignx center, aligny center");
			}
			this.add(bottomPanel, BorderLayout.SOUTH);
		}

		// Resize window
		this.setMinimumSize(new Dimension(630, 400));
		this.setSize(new Dimension(630, 500));

		// Center window
		this.setLocationRelativeTo(null);

		initListeners();
	}

	private void initListeners() {
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				QueryWindow.this.setVisible(false);
			}

		});

		submitButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				QueryWindow.this.setVisible(false);
				taskManager.execute(createNetworkFactory.createTaskIterator());
			}

		});
	}

	public void updateLists(List<PsicquicService> databases, List<Organism> organisms) {
		for (NetworkQueryPanel networkQueryPanel : networkQueryPanels.getTabPanels())
			networkQueryPanel.updateLists(databases, organisms);
	}

	@Override
	public List<PsicquicService> getSelectedDatabases() {
		return networkQueryPanels.getActivePanel().getSelectedDatabases();
	}

	@Override
	public Organism getReferenceOrganism() {
		return networkQueryPanels.getActivePanel().getReferenceOrganism();
	}

	@Override
	public List<String> getProteinOfInterestUniprotId() {
		return networkQueryPanels.getActivePanel().getProteinOfInterestUniprotId();
	}

	@Override
	public List<Organism> getOtherOrganisms() {
		return networkQueryPanels.getActivePanel().getOtherOrganisms();
	}

	public void setCreateNetworkFactory(PMBInteractionNetworkBuildTaskFactory createNetworkFactory) {
		this.createNetworkFactory = createNetworkFactory;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			submitButton.grabFocus();
			getRootPane().setDefaultButton(submitButton);
			repaint();
		}
	}

	@Override
	public void gainedFocus() {
		bottomPanel.setBackground(PMBUIStyle.focusActiveTabColor);
	}

	@Override
	public void lostFocus() {
		bottomPanel.setBackground(PMBUIStyle.blurActiveTabColor);
	}
}

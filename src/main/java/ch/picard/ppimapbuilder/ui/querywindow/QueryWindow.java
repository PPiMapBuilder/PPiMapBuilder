package ch.picard.ppimapbuilder.ui.querywindow;

import ch.picard.ppimapbuilder.data.interaction.client.web.PsicquicService;
import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import ch.picard.ppimapbuilder.ui.querywindow.component.ChangingColorComponentUpdater;
import ch.picard.ppimapbuilder.ui.querywindow.component.ChangingColorPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.CustomSplitPane;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.DatabaseSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.OtherOrganismSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.ReferenceOrganismSelectionPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.UniprotSelection;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame implements NetworkQueryParameters {

	/**
	 * Instance of the PPiMapBuilder frame to prevent several instances
	 */
	private static final long serialVersionUID = 1L;

	// Task management
	private PMBInteractionNetworkBuildTaskFactory createNetworkFactory;
	private TaskManager taskManager;

	// The create network window
	//private JFrame window;

	// Databases and organism panels containing all checkbox
	private UniprotSelection uus;
	private DatabaseSelectionPanel dsp;
	private OtherOrganismSelectionPanel ogs;
	private ReferenceOrganismSelectionPanel org;

	// Fancy design element
	private final ChangingColorComponentUpdater changingColorComponentUpdater;
	private final Color blurDarkPanelColor;
	private final Color focusDarkerPanelColor;

	private JButton startQuery;
	private JButton cancel;

	/**
	 * Create the network creation frame
	 */
	public QueryWindow() {
		//window = new JFrame("PPiMapBuilder - Create a network");
		super("PPiMapBuilder - Create a network");

		// Slightly darker color than window background color
		Color defaultPanelColor = UIManager.getColor("Panel.background");
		float hsbVals[] = Color.RGBtoHSB(defaultPanelColor.getRed(), defaultPanelColor.getGreen(), defaultPanelColor.getBlue(), null);
		blurDarkPanelColor = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.97f * hsbVals[2]);
		focusDarkerPanelColor = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.9f * hsbVals[2]);

		addWindowFocusListener(changingColorComponentUpdater = new ChangingColorComponentUpdater());

		// Create all component in the window
		initialize();
	}

	/**
	 * Initialize the contents of the frame
	 */
	private void initialize() {
		// Split panel
		CustomSplitPane splitPane = new CustomSplitPane(changingColorComponentUpdater, focusDarkerPanelColor, blurDarkPanelColor);
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

		// Left part
		splitPane.setLeftComponent(
				uus = new UniprotSelection()
		);

		// Right part
		splitPane.setRightComponent(
				initMainFormPanel()
		);

		// Bottom part
		JPanel panBottomForm = initBottomPanel();
		this.getContentPane().add(panBottomForm, BorderLayout.SOUTH);

		initListeners();

		// Resize window
		this.setMinimumSize(new Dimension(630, 400));
		this.setSize(new Dimension(630, 500));

		// Center window
		this.setLocationRelativeTo(null);
	}

	/**
	 * Creating the main form panel containing the organism selector and the
	 * source database selector
	 *
	 * @return the generated JPanel
	 */
	private JPanel initMainFormPanel() {
		// Main form panel
		JPanel panMainForm = new JPanel();
		panMainForm.setBorder(PMBUIStyle.fancyPanelBorderWithPadding);
		panMainForm.setMinimumSize(new Dimension(440, 10));

		panMainForm.setLayout(new MigLayout("inset 10", "[49.00,grow][14px:14px:14px,right]", "[][][][grow][][45%]"));

		org = new ReferenceOrganismSelectionPanel(this, panMainForm);

		ogs = new OtherOrganismSelectionPanel(panMainForm, PMBUIStyle.fancyPanelBorder);

		dsp = new DatabaseSelectionPanel(panMainForm, PMBUIStyle.fancyPanelBorder);

		return panMainForm;
	}

	public void updateLists(List<PsicquicService> databases, List<Organism> organisms) {
		dsp.updateList(databases);
		ogs.updateList(organisms);
		org.updateList(organisms);
	}

	/**
	 * Creating bottom panel with cancel and submit button
	 *
	 * @return the generated JPanel
	 */
	private JPanel initBottomPanel() {
		//Bottom Panel
		JPanel panBottomForm = new ChangingColorPanel(
				changingColorComponentUpdater,
				focusDarkerPanelColor,
				blurDarkPanelColor,
				PMBUIStyle.emptyBorder,
				null
		);
		panBottomForm.setLayout(new MigLayout("inset 5", "[grow][100px][][100px]", "[29px]"));
		panBottomForm.setPreferredSize(new Dimension(0, 42));

		//Cancel Button
		cancel = new JButton("Cancel");
		cancel.setMnemonic(KeyEvent.VK_CANCEL);
		//Add cancel to panel
		panBottomForm.add(cancel, "cell 1 0,alignx center,aligny center");

		//Submit Button
		startQuery = new JButton("Submit");
		//Add submit to panel
		panBottomForm.add(startQuery, "cell 3 0,alignx center,aligny center");

		return panBottomForm;
	}

	private void initListeners() {
		startQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				QueryWindow.this.setVisible(false);

				taskManager.execute(createNetworkFactory.createTaskIterator());
			}

		});
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				QueryWindow.this.setVisible(false);
			}

		});
	}

	@Override
	public List<PsicquicService> getSelectedDatabases() {
		return dsp.getSelectedDatabases();
	}

	@Override
	public Organism getReferenceOrganism() {
		return org.getSelectedOrganism();
	}

	@Override
	public List<String> getProteinOfInterestUniprotId() {
		return uus.getIdentifiers();
	}

	@Override
	public List<Organism> getOtherOrganisms() {
		return ogs.getSelectedOrganisms();
	}

	public void setCreateNetworkFactory(PMBInteractionNetworkBuildTaskFactory createNetworkFactory) {
		this.createNetworkFactory = createNetworkFactory;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public OtherOrganismSelectionPanel getOtherOrganismSelectionPanel() {
		return ogs;
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b) {
			startQuery.grabFocus();
			this.getRootPane().setDefaultButton(startQuery);
			repaint();
		}
	}

}

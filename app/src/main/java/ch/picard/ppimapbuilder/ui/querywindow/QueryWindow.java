/*   
 * This file is part of PPiMapBuilder.
 *
 * PPiMapBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PPiMapBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PPiMapBuilder.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 Echeverria P.C., Dupuis P., Cornut G., Gravouil K., Kieffer A., Picard D.
 * 
 */    	
    
package ch.picard.ppimapbuilder.ui.querywindow;

import ch.picard.ppimapbuilder.data.organism.Organism;
import ch.picard.ppimapbuilder.networkbuilder.NetworkQueryParameters;
import ch.picard.ppimapbuilder.networkbuilder.PMBInteractionNetworkBuildTaskFactory;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.InteractomeNetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.NetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.querywindow.component.panel.ProteinNetworkQueryPanel;
import ch.picard.ppimapbuilder.ui.util.FocusPropagator;
import ch.picard.ppimapbuilder.ui.util.PMBUIStyle;
import ch.picard.ppimapbuilder.ui.util.tabpanel.TabPanel;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame implements NetworkQueryParameters, FocusListener {

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
		focusPropagator.add(this);
		this.addWindowFocusListener(focusPropagator);

		{ // Init components
			this.setLayout(new BorderLayout());

			// Protein network panel
			ProteinNetworkQueryPanel proteinNetworkQueryPanel =
					new ProteinNetworkQueryPanel();
			focusPropagator.add(proteinNetworkQueryPanel);

			// Interactome network panel
			InteractomeNetworkQueryPanel interactomeNetworkQueryPanel =
					new InteractomeNetworkQueryPanel();
			focusPropagator.add(interactomeNetworkQueryPanel);

			// Tab panel with protein and interactome network query panels
			networkQueryPanels = new TabPanel<NetworkQueryPanel>(
					proteinNetworkQueryPanel,
					interactomeNetworkQueryPanel
			);
			focusPropagator.add(networkQueryPanels);
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

	public void updateDatabases(List<Map> databases) {
		for (NetworkQueryPanel networkQueryPanel : networkQueryPanels.getTabPanels())
			networkQueryPanel.updateDatabases(databases);
	}

	public void updateOrganisms(List<Organism> organisms) {
		for (NetworkQueryPanel networkQueryPanel : networkQueryPanels.getTabPanels())
			networkQueryPanel.updateOrganisms(organisms);
	}

	@Override
	public List<Map> getSelectedDatabases() {
		return networkQueryPanels.getActivePanel().getSelectedDatabases();
	}

	@Override
	public boolean isInteractomeQuery() {
		return networkQueryPanels.getActivePanel().isInteractomeQuery();
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
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			submitButton.grabFocus();
			getRootPane().setDefaultButton(submitButton);
			repaint();
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		bottomPanel.setBackground(PMBUIStyle.focusActiveTabColor);
	}

	@Override
	public void focusLost(FocusEvent e) {
		bottomPanel.setBackground(PMBUIStyle.blurActiveTabColor);
	}
}
